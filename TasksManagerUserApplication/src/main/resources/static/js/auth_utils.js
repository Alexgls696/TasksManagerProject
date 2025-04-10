// В вашем файле auth.js или аналогичном

const API_GATEWAY_BASE_URL = 'http://localhost:8080'; // Адрес API Gateway
const SECURITY_SERVICE_BASE_URL = ''; // Адрес вашего сервиса безопасности (если он отличается от UI)
// Если UI и Security на одном домене/порту, можно оставить пустым или '/'
const storageKey = 'accessToken';
let isRefreshing = false; // Флаг, чтобы избежать одновременных запросов на обновление
let failedQueue = []; // Очередь запросов, которые ждали обновления токена

const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

async function refreshToken() {
    console.log("Попытка обновить токен...");
    try {
        // ВАЖНО: Запрос идет на ваш СЕРВИС БЕЗОПАСНОСТИ, а не на API Gateway
        // Этот запрос будет использовать SESSION куку для аутентификации на бэкенде
        const refreshUrl = `${SECURITY_SERVICE_BASE_URL}/api/refresh-token`;
        const response = await fetch(refreshUrl, {
            method: 'POST',
            // НЕ НУЖЕН 'Authorization': Bearer здесь, используется сессионная кука
            headers: {
                'Content-Type': 'application/json', // Хотя тело пустое, может требоваться
                'Accept': 'application/json',
                // Добавьте 'X-Requested-With': 'XMLHttpRequest', если ваш бэкенд это требует для AJAX
                'X-Requested-With': 'XMLHttpRequest'
            },
        });

        if (response.ok) {
            const data = await response.json();
            const newAccessToken = data.access_token;
            if (newAccessToken) {
                console.log("Токен успешно обновлен.");
                localStorage.setItem(storageKey, newAccessToken);
                // (Опционально) Сохранить время истечения data.expires_at для проактивного обновления
                return newAccessToken;
            } else {
                throw new Error("Новый токен не пришел в ответе.");
            }
        } else {
            // Если бэкенд вернул ошибку (например, 401 - Refresh Token истек)
            console.error("Ошибка обновления токена:", response.status, await response.text());
            throw new Error(`Refresh failed with status ${response.status}`);
        }
    } catch (error) {
        console.error("Критическая ошибка при обновлении токена:", error);
        // Если обновление не удалось, выходим из системы
        handleLogout(); // Вызываем выход, так как сессия недействительна
        throw error; // Пробрасываем ошибку дальше, чтобы запросы, ожидавшие токен, упали
    }
}


// --- Внутренняя функция (не экспортируем, так как используется только здесь) ---
function getAccessToken() {
    return localStorage.getItem(storageKey);
}

/**
 * Обертка для fetch, которая автоматически добавляет заголовок Authorization
 * и пытается обновить токен при 401 ошибке.
 * @param {string} url - URL эндпоинта (относительный путь к API Gateway или полный URL)
 * @param {object} options - Настройки fetch (method, body, headers и т.д.)
 * @param {boolean} isRetry - Флаг, указывающий, что это повторный запрос после обновления токена
 * @returns {Promise<Response>} Промис с ответом fetch.
 */
export async function fetchWithAuth(url, options = {}, isRetry = false) {
    let token = getAccessToken();

    const headers = {
        ...(options.headers || {}),
    };
    if (!headers['Content-Type'] && ['POST', 'PUT', 'PATCH'].includes(options.method?.toUpperCase())) {
        headers['Content-Type'] = 'application/json';
    }
    if (!headers['Accept']) {
        headers['Accept'] = 'application/json';
    }
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const fetchOptions = {
        ...options,
        headers: headers,
    };

    // Формируем полный URL для API GATEWAY
    const fullUrl = url.startsWith('http') ? url : `${API_GATEWAY_BASE_URL}${url}`;
    console.log(`Выполняется запрос ${fetchOptions.method || 'GET'} на ${fullUrl}` + (isRetry ? ' (Retry)' : ''));

    try {
        const response = await fetch(fullUrl, fetchOptions);

        // ----- ЛОВИМ 401 -----
        if (response.status === 401 && !isRetry) {
            console.warn(`Запрос на ${fullUrl} вернул 401. Попытка обновить токен...`);

            if (!isRefreshing) { // Если никто еще не обновляет токен
                isRefreshing = true;
                try {
                    const newToken = await refreshToken(); // Вызываем функцию обновления
                    processQueue(null, newToken); // Уведомляем все ждущие запросы об успехе
                    // Повторяем ИСХОДНЫЙ запрос с НОВЫМ токеном
                    // Передаем isRetry = true, чтобы избежать бесконечного цикла
                    return fetchWithAuth(url, options, true);
                } catch (refreshError) {
                    console.error("Не удалось обновить токен, выход из системы.", refreshError);
                    processQueue(refreshError, null); // Уведомляем об ошибке
                    // handleLogout() уже вызван внутри refreshToken при ошибке
                    throw refreshError; // Прерываем выполнение
                } finally {
                    isRefreshing = false;
                }
            } else {
                // Если кто-то уже обновляет, добавляем промис в очередь
                console.log("Обновление токена уже в процессе, ожидаем...");
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                }).then(newToken => {
                    // Когда токен обновился, повторяем запрос
                    return fetchWithAuth(url, options, true);
                }).catch(err => {
                    // Если обновление не удалось у другого запроса
                    throw err; // Пробрасываем ошибку
                });
            }

        } else if (response.status === 401 && isRetry) {
            // Получили 401 даже после попытки обновления - значит что-то не так
            console.error("Получен 401 даже после попытки обновления токена. Выход.");
            handleLogout();
            // Можно выбросить специальную ошибку или просто вернуть response
            throw new Error("Authentication failed after token refresh attempt.");
        }

        // Обработка других статусов (403 Forbidden и т.д.)
        if (response.status === 403) {
            console.warn(`Запрос на ${fullUrl} вернул 403 Forbidden. Недостаточно прав.`);
            // Здесь не нужно обновлять токен, проблема в правах доступа
        }

        return response; // Возвращаем оригинальный ответ, если не было 401 или это был успешный retry

    } catch (error) {
        // Ловим ошибки сети и другие ошибки fetch/refresh
        if (error.message.includes("Authentication failed")) { // Если ошибка пришла из нашей логики 401
            throw error;
        }
        console.error(`Ошибка сети или выполнения при запросе на ${fullUrl}:`, error);
        throw error; // Пробрасываем ошибку дальше
    }
}

/**
 * Выполняет выход пользователя: очищает локальный токен и перенаправляет на бэкенд.
 */
export function handleLogout() {
    console.log("Выполняется выход пользователя...");
    localStorage.removeItem(storageKey); // Очищаем старый токен
    console.log("Локальный токен очищен.");
    // Перенаправляем на эндпоинт logout на СЕРВЕРЕ БЕЗОПАСНОСТИ
    window.location.href = `${SECURITY_SERVICE_BASE_URL}/security/logout`; // Используем URL сервера безопасности
}


// --- Инициализация (код, который должен выполниться при загрузке модуля) ---
// ... (ваш существующий код для привязки handleLogout к кнопке/ссылке) ...
// Например:
document.addEventListener("DOMContentLoaded", function () {
    const logoutElement = document.getElementById('login_or_logout') || document.getElementById('logout-button');
    if (logoutElement) {
        logoutElement.addEventListener('click', function(event) {
            event.preventDefault();
            handleLogout();
        });
        console.log("Обработчик выхода привязан.");
    }
});