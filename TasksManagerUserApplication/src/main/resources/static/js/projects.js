const API_GATEWAY_BASE_URL = 'http://localhost:8080'; // URL вашего API Gateway
const storageKey = 'accessToken'; // Тот же ключ, что и при сохранении

/**
 * Получает сохраненный токен доступа.
 * @returns {string|null} Токен или null, если не найден.
 */
function getAccessToken() {
    // Используйте тот же storage, что и в login-callback
    return sessionStorage.getItem(storageKey);
    // или return localStorage.getItem(storageKey);
}

/**
 * Обертка для fetch, которая автоматически добавляет заголовок Authorization.
 * @param {string} url - URL эндпоинта (относительный путь к API Gateway)
 * @param {object} options - Настройки fetch (method, body, headers и т.д.)
 * @returns {Promise<Response>} Промис с ответом fetch.
 */
async function fetchWithAuth(url, options = {}) {
    const token = getAccessToken();
    const headers = {
        // Копируем существующие заголовки из options
        ...(options.headers || {}),
    };

    // Добавляем Content-Type по умолчанию для POST/PUT, если он не задан
    if (!headers['Content-Type'] && ['POST', 'PUT', 'PATCH'].includes(options.method?.toUpperCase())) {
        headers['Content-Type'] = 'application/json';
    }
    // Добавляем Accept по умолчанию, если он не задан
    if (!headers['Accept']) {
        headers['Accept'] = 'application/json';
    }

    // Добавляем токен в заголовок Authorization, если он есть
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const fetchOptions = {
        ...options,
        headers: headers,
    };

    // Формируем полный URL
    const fullUrl = url.startsWith('http') ? url : `${API_GATEWAY_BASE_URL}${url}`;

    console.log(`Выполняется запрос ${fetchOptions.method || 'GET'} на ${fullUrl}`);

    try {
        const response = await fetch(fullUrl, fetchOptions);

        // Базовая обработка неавторизованных запросов
        if (response.status === 401 || response.status === 403) {
            console.warn(`Запрос на ${fullUrl} вернул ${response.status}. Возможно, токен истек или недействителен.`);
            // Здесь можно добавить логику для выхода пользователя или попытки обновления токена
            // Например:
            // clearTokenAndRedirectToLogin();
        }

        return response;
    } catch (error) {
        console.error(`Ошибка сети при запросе на ${fullUrl}:`, error);
        throw error; // Перебрасываем ошибку для дальнейшей обработки
    }
}
//-----------------------------------------------------------------------------------------------------------------------

document.addEventListener("DOMContentLoaded", function () {
    // Загружаем проекты
    loadProjects();

    // Функция для загрузки проектов
    async function loadProjects() {
        try {
            const projects = await fetchProjects();
            displayProjects(projects);
        } catch (error) {
            console.error("Ошибка при загрузке проектов:", error);
            showError("Не удалось загрузить проекты");
        }
    }

    // Функция для получения проектов
    async function fetchProjects() {
        const response = await fetchWithAuth("/task-manager-api/projects"); //await fetch("http://localhost:8080/task-manager-api/projects");
        console.log(response)
        if (!response.ok) {
            throw new Error("Ошибка при загрузке проектов");
        }
        return response.json();
    }

    // Функция для отображения проектов
    function displayProjects(projects) {
        const projectsGrid = document.getElementById('projects-grid');
        projectsGrid.innerHTML = '';

        if (!projects || projects.length === 0) {
            projectsGrid.innerHTML = `
                <div class="empty-projects">
                    <i class="fas fa-project-diagram"></i>
                    <p>У вас пока нет проектов</p>
                    <a href="/create-project" class="btn-primary"><i class="fas fa-plus"></i> Создать первый проект</a>
                </div>
            `;
            return;
        }

        projects.forEach(project => {
            const projectElement = document.createElement('div');
            projectElement.classList.add('project-card');
            projectElement.setAttribute('data-project-id', project.id);

            // Форматирование дат
            const creationDate = formatDate(project.creationDate);
            const deadline = formatDate(project.deadline);

            // Определяем класс статуса
            let statusClass = 'project-status';
            if (project.projectStatus) {
                const status = project.projectStatus.status.toLowerCase();
                if (status.includes('актив')) statusClass += ' status-active';
                else if (status.includes('заверш')) statusClass += ' status-completed';
                else if (status.includes('архив')) statusClass += ' status-archived';
            }

            projectElement.innerHTML = `
                <div class="${statusClass}">${project.projectStatus?.status || 'Без статуса'}</div>
                <h3><a href="/tasks?projectId=${project.id}">${project.name || 'Без названия'}</a></h3>
                <div class="project-meta">
                    <p><strong><i class="fas fa-align-left"></i> Описание:</strong> ${project.description || 'Нет описания'}</p>
                    <p><strong><i class="fas fa-calendar-times"></i> Дедлайн:</strong> ${deadline}</p>
                    <p><strong><i class="fas fa-user-edit"></i> Создатель:</strong> ${project.creator?.name || 'Неизвестно'} ${project.creator?.surname || ''}</p>
                </div>
                <div class="project-actions">
                    <button class="btn-edit"><i class="fas fa-edit"></i> Редактировать</button>
                    <button class="btn-delete"><i class="fas fa-trash"></i> Удалить</button>
                </div>
            `;

            projectsGrid.appendChild(projectElement);
        });

        // Назначаем обработчики для кнопок
        setupProjectActions();
    }

    // Функция для форматирования даты
    function formatDate(dateString) {
        if (!dateString) return 'Не указана';
        const date = new Date(dateString);
        return new Intl.DateTimeFormat('ru-RU', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        }).format(date);
    }

    // Настройка обработчиков для кнопок проектов
    function setupProjectActions() {
        // Обработчик для кнопки "Редактировать"
        document.querySelectorAll('.btn-edit').forEach(button => {
            button.addEventListener('click', function(event) {
                event.stopPropagation();
                const projectId = this.closest('.project-card').getAttribute('data-project-id');
                if (projectId) {
                    window.location.href = `/edit-project?id=${projectId}`;
                } else {
                    showError('Ошибка: ID проекта не найден');
                }
            });
        });

        // Обработчик для кнопки "Удалить"
        document.querySelectorAll('.btn-delete').forEach(button => {
            button.addEventListener('click', function(event) {
                event.stopPropagation();
                const projectElement = this.closest('.project-card');
                const projectId = projectElement.getAttribute('data-project-id');

                if (projectId) {
                    confirmDeleteProject(projectId, projectElement);
                } else {
                    showError('Ошибка: ID проекта не найден');
                }
            });
        });
    }

    // Подтверждение удаления проекта
    function confirmDeleteProject(projectId, projectElement) {
        Swal.fire({
            icon: 'warning',
            title: 'Вы уверены?',
            text: 'Вы действительно хотите удалить этот проект? Все связанные задачи также будут удалены!',
            showCancelButton: true,
            confirmButtonText: 'Да, удалить',
            cancelButtonText: 'Отмена',
            confirmButtonColor: '#dc3545'
        }).then((result) => {
            if (result.isConfirmed) {
                deleteProject(projectId, projectElement);
            }
        });
    }

    // Удаление проекта
    async function deleteProject(projectId, projectElement) {
        try {
            const response = await fetch(`http://localhost:8080/task-manager-api/projects/${projectId}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                projectElement.remove();
                showSuccess('Проект успешно удален');

                // Проверяем, остались ли еще проекты
                const projectsGrid = document.getElementById('projects-grid');
                if (projectsGrid.children.length === 0) {
                    projectsGrid.innerHTML = `
                        <div class="empty-projects">
                            <i class="fas fa-project-diagram"></i>
                            <p>У вас пока нет проектов</p>
                            <a href="/create-project" class="btn-primary"><i class="fas fa-plus"></i> Создать первый проект</a>
                        </div>
                    `;
                }
            } else {
                throw new Error('Ошибка сервера');
            }
        } catch (error) {
            console.error('Ошибка при удалении проекта:', error);
            showError('Не удалось удалить проект');
        }
    }

    // Функция для показа успешного уведомления
    function showSuccess(message) {
        Swal.fire({
            icon: 'success',
            title: 'Успех!',
            text: message,
            confirmButtonText: 'ОК'
        });
    }

    // Функция для показа уведомления об ошибке
    function showError(message) {
        Swal.fire({
            icon: 'error',
            title: 'Ошибка!',
            text: message,
            confirmButtonText: 'ОК'
        });
    }
});