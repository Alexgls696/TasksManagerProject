const API_GATEWAY_BASE_URL = 'http://localhost:8080'; // Адрес API Gateway (куда идут обычные API запросы)
// ВАЖНО: Адрес вашего сервиса безопасности, ГДЕ находится /api/refresh-token и /logout
// Если UI и Security Service на одном домене/порту (например, все на localhost:8081),
// можно оставить пустым или '/', браузер сам подставит текущий origin.
// Если они на разных (UI на :3000, Security на :8081), укажите полный адрес Security Service.
// const SECURITY_SERVICE_BASE_URL = 'http://localhost:8081'; // Пример, если они разделены
const SECURITY_SERVICE_BASE_URL = 'http://localhost:8080'; // Используем пустую строку, если UI и Security на одном origin

const storageKey = 'accessToken';
let isRefreshing = false; // Флаг, чтобы избежать одновременных запросов на обновление
let failedQueue = []; // Очередь запросов, которые ждали обновления токена

const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token); // Передаем новый токен ожидающим промисам
        }
    });
    failedQueue = [];
};

/**
 * Выполняет выход пользователя: очищает локальный токен и перенаправляет на бэкенд.
 */
export function handleLogout() {
    console.log("Выполняется выход пользователя...");
    localStorage.removeItem(storageKey); // Очищаем старый токен
    console.log("Локальный токен очищен.");
    window.location.href = `${SECURITY_SERVICE_BASE_URL}/security/logout`;
}

async function refreshToken() {
    console.log("Попытка обновить токен...");
    try {
        const refreshUrl = `${SECURITY_SERVICE_BASE_URL}/security/api/refresh-token`; // Путь к эндпоинту обновления
        const response = await fetch(refreshUrl, {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Accept': 'application/json',
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
                return newAccessToken; // Возвращаем новый токен для повторного запроса
            } else {
                console.error("Новый токен не пришел в ответе от /api/refresh-token.");
                throw new Error("Новый токен не пришел в ответе.");
            }
        } else {
            // Если бэкенд вернул ошибку (например, 401 - Refresh Token истек или сессия невалидна)
            console.error("Ошибка обновления токена (сервер вернул ошибку):", response.status, await response.text());
            throw new Error(`Refresh failed with status ${response.status}`);
        }
    } catch (error) {
        console.error("Критическая ошибка при вызове /api/refresh-token:", error);
        // Если обновление не удалось (сетевая ошибка или серверная), выходим из системы
        //handleLogout(); // Вызываем выход, так как сессия, скорее всего, недействительна
        throw error; // Пробрасываем ошибку дальше, чтобы запросы, ожидавшие токен, упали
    }
}

// --- Внутренняя функция ---
function getAccessToken() {
    return localStorage.getItem(storageKey);
}

/**
 * Обертка для fetch, которая автоматически добавляет заголовок Authorization
 * и пытается обновить токен при 401 ошибке.
 * @param {string} url - URL эндпоинта (относительный путь к API Gateway или полный URL)
 * @param {object} options - Настройки fetch (method, body, headers и т.д.)
 * @param {boolean} [isRetry=false] - Флаг, указывающий, что это повторный запрос после обновления токена
 * @returns {Promise<Response>} Промис с ответом fetch.
 */
export async function fetchWithAuth(url, options = {}, isRetry = false) {
    let token = getAccessToken();

    const headers = {
        ...(options.headers || {}), // Копируем существующие заголовки
    };
    // Устанавливаем Content-Type по умолчанию для POST/PUT/PATCH, если не задан
    if (!headers['Content-Type'] && ['POST', 'PUT', 'PATCH'].includes(options.method?.toUpperCase())) {
        headers['Content-Type'] = 'application/json';
    }
    // Устанавливаем Accept по умолчанию, если не задан
    if (!headers['Accept']) {
        headers['Accept'] = 'application/json';
    }
    // Добавляем токен авторизации, если он есть
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const fetchOptions = {
        ...options,
        headers: headers,
    };

    // Формируем полный URL для запроса к API GATEWAY
    const fullUrl = url.startsWith('http') ? url : `${API_GATEWAY_BASE_URL}${url}`;
    try {
        const response = await fetch(fullUrl, fetchOptions);

        // ----- ПЕРЕХВАТ 401 ОШИБКИ -----
        if (response.status === 401 && !isRetry) {
            console.warn(`Запрос на ${fullUrl} вернул 401. Попытка обновить токен...`);

            if (!isRefreshing) {
                isRefreshing = true;
                try {
                    const newToken = await refreshToken();
                    console.log("Обновление завершено, обрабатываем очередь и повторяем запрос.");
                    processQueue(null, newToken);
                    fetchOptions.headers['Authorization'] = `Bearer ${newToken}`;
                    return fetchWithAuth(url, fetchOptions, true);
                } catch (refreshError) {
                    console.error("Не удалось обновить токен:", refreshError);
                    processQueue(refreshError, null); // Уведомляем об ошибке
                    // handleLogout() уже вызван внутри refreshToken при критической ошибке
                    throw refreshError; // Прерываем выполнение текущего запроса
                } finally {
                    isRefreshing = false;
                    console.log("Флаг isRefreshing сброшен.");
                }
            } else {
                // Если кто-то уже обновляет, добавляем промис в очередь
                console.log("Обновление токена уже в процессе, добавляем запрос в очередь ожидания...");
                return new Promise((resolve, reject) => {
                    // Когда processQueue вызовется, этот промис разрешится или отклонится
                    failedQueue.push({ resolve, reject });
                }).then(newToken => {
                    // Когда токен обновился, повторяем исходный запрос с новым токеном
                    console.log("Очередь обработана, повторяем запрос из очереди с новым токеном.");
                    fetchOptions.headers['Authorization'] = `Bearer ${newToken}`;
                    return fetchWithAuth(url, fetchOptions, true); // ВАЖНО: передаем обновленные fetchOptions
                }).catch(err => {
                    // Если обновление не удалось у "главного" запроса
                    console.error("Обновление токена провалилось, запрос из очереди также завершается с ошибкой.");
                    throw err; // Пробрасываем ошибку
                });
            }

        } else if (response.status === 401 && isRetry) {
            // Получили 401 даже после попытки обновления - значит что-то серьезно не так
            // (например, бэкенд некорректно выдал токен или права изменились мгновенно)
            console.error("Получен 401 даже после успешной попытки обновления токена. Выход.");
            //handleLogout();
            // Выбрасываем ошибку, чтобы вызывающий код мог ее обработать
            throw new Error("Authentication failed after token refresh attempt.");
        }
        return response;

    } catch (error) {
        // Ловим ошибки сети и другие ошибки fetch/refresh
        // Проверяем, не была ли это ошибка, которую мы сами бросили
        if (error.message.includes("Authentication failed") || error.message.includes("Refresh failed") || error.message.includes("Новый токен не пришел")) {
            // Эти ошибки уже обработаны (включая logout, если нужно), просто пробрасываем
            throw error;
        }
        // Иначе это, скорее всего, сетевая ошибка
        console.error(`Ошибка сети или выполнения при запросе на ${fullUrl}:`, error);
        throw error; // Пробрасываем ошибку дальше
    }
}


// --- Пример использования ---
async function getUserProfile() {
    try {
        const response = await fetchWithAuth('/profile'); // Вызываем защищенный эндпоинт
        if (!response.ok) {
            // Обрабатываем другие ошибки (403, 500 и т.д.)
            console.error("Не удалось загрузить профиль:", response.status);
            const errorData = await response.text(); // или .json()
            console.error("Тело ошибки:", errorData)
            return null;
        }
        const profile = await response.json();
        console.log("Профиль пользователя:", profile);
        return profile;
    } catch (error) {
        // Ловим ошибки, включая ошибки аутентификации/обновления
        console.error("Итоговая ошибка при получении профиля:", error);
        // Здесь можно показать сообщение пользователю
        return null;
    }
}

// Вызовите getUserProfile() там, где вам нужно получить данные

// --- Инициализация обработчика кнопки выхода ---
document.addEventListener("DOMContentLoaded", function () {
    // Используйте селектор, который точно соответствует вашей кнопке/ссылке выхода
    const logoutElement = document.getElementById('exit-button') // Пример селекторов
    if (logoutElement) {
        logoutElement.addEventListener('click', function(event) {
            event.preventDefault(); // Предотвращаем стандартный переход по ссылке, если это <a>
            handleLogout();
        });
        console.log("Обработчик выхода привязан к элементу:", logoutElement);
    } else {
        console.warn("Элемент для выхода не найден на странице.");
    }
});
