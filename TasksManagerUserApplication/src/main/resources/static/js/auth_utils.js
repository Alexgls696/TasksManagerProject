// Файл: js/authUtils.js (или другое подходящее имя и путь)

// --- Константы (экспортируем, только если они нужны напрямую в других модулях) ---
// Обычно базовый URL лучше скрыть внутри fetchWithAuth
const API_GATEWAY_BASE_URL = 'http://localhost:8080';
const storageKey = 'accessToken';

// --- Внутренняя функция (не экспортируем, так как используется только здесь) ---
function getAccessToken() {
    return localStorage.getItem(storageKey);
}

// --- ЭКСПОРТИРУЕМЫЕ Функции ---

/**
 * Обертка для fetch, которая автоматически добавляет заголовок Authorization.
 * @param {string} url - URL эндпоинта (относительный путь к API Gateway или полный URL)
 * @param {object} options - Настройки fetch (method, body, headers и т.д.)
 * @returns {Promise<Response>} Промис с ответом fetch.
 */
export async function fetchWithAuth(url, options = {}) {
    const token = getAccessToken(); // Используем внутреннюю функцию
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

    // Формируем полный URL, используя внутреннюю константу
    const fullUrl = url.startsWith('http') ? url : `${API_GATEWAY_BASE_URL}${url}`;

    console.log(`Выполняется запрос ${fetchOptions.method || 'GET'} на ${fullUrl}`);

    try {
        const response = await fetch(fullUrl, fetchOptions);
        if (response.status === 401 || response.status === 403) {
            console.warn(`Запрос на ${fullUrl} вернул ${response.status}. Возможно, токен истек или недействителен.`);
            // Раскомментируйте, если нужна автоматическая очистка и редирект при 401/403
            // handleLogout();
        }
        return response;
    } catch (error) {
        console.error(`Ошибка сети при запросе на ${fullUrl}:`, error);
        throw error;
    }
}

/**
 * Выполняет выход пользователя: очищает локальный токен и перенаправляет на бэкенд.
 */
export function handleLogout() {
    console.log("Выполняется выход пользователя...");
    localStorage.removeItem(storageKey); // Используем внутреннюю константу
    console.log("Локальный токен очищен.");
    // Перенаправляем на эндпоинт logout на шлюзе
    window.location.href = '/security/logout'; // Относительный путь к шлюзу
}


// --- Инициализация (код, который должен выполниться при загрузке модуля) ---

// Добавляем обработчик на кнопку выхода, если она есть на странице
document.addEventListener("DOMContentLoaded", function () {
    const logoutButton = document.getElementById('logout-button');
    if (logoutButton) {
        // Используем экспортированную функцию
        logoutButton.addEventListener('click', handleLogout);
    } else {
        // Можно убрать этот лог, если модуль будет подключаться на страницах без кнопки
        // console.warn("Auth Utils: Кнопка выхода не найдена на этой странице.");
    }
});

document.addEventListener("DOMContentLoaded", function () {
    // Ищем элемент по ID, который вы используете в HTML
    const logoutLink = document.getElementById('login_or_logout'); // <--- Ищем правильный ID

    if (logoutLink) {
        // Привязываем функцию handleLogout к событию 'click'
        logoutLink.addEventListener('click', function(event) {
            event.preventDefault(); // Предотвращаем стандартный переход по ссылке '#'
            handleLogout();       // Вызываем нашу функцию
        });
        console.log("Обработчик выхода привязан к элементу #login_or_logout.");
    } else {
        console.warn("Auth Utils: Ссылка для выхода (#login_or_logout) не найдена на этой странице.");
    }
});