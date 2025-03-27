document.addEventListener("DOMContentLoaded", function () {
    // Получаем ID проекта из URL
    const urlParams = new URLSearchParams(window.location.search);
    const projectId = urlParams.get("projectId");

    if (!projectId) {
        Swal.fire({
            icon: 'error',
            title: 'Ошибка',
            text: 'ID проекта не указан',
            confirmButtonText: 'OK'
        }).then(() => {
            window.location.href = "/index";
        });
        return;
    }

    // Устанавливаем projectId в скрытое поле формы
    document.getElementById('project_id_input').value = projectId;

    // Загружаем данные проекта
    loadProjectData(projectId)
        .then(project => {
            document.getElementById('project-title').textContent = project.name;
            document.getElementById('project_name_input').value = project.name;
        })
        .catch(error => {
            console.error("Ошибка загрузки данных проекта:", error);
            document.getElementById('project-title').textContent = "Неизвестный проект";
        });

    // Загружаем задачи для проекта
    loadTasks(projectId);

    // Обработчик кнопки "Добавить задачу"
    document.getElementById('add-task-btn').addEventListener('click', function(event) {
        event.preventDefault();
        document.getElementById('create-task-form').submit();
    });

    // Обработчик кнопки "Назад к проектам"
    document.getElementById('back-to-projects').addEventListener('click', function(event) {
        event.preventDefault();
        window.location.href = "/index";
    });
});

// Функция для загрузки данных проекта
async function loadProjectData(projectId) {
    const response = await fetch(`http://localhost:8080/task-manager-api/projects/${projectId}`);
    if (!response.ok) {
        throw new Error("Ошибка при загрузке данных проекта");
    }
    return response.json();
}

// Функция для загрузки задач проекта
async function loadTasks(projectId) {
    try {
        const tasks = await fetchTasksByProjectId(projectId);
        displayTasks(tasks);
    } catch (error) {
        console.error("Ошибка при загрузке задач:", error);
        showError("Не удалось загрузить задачи");
    }
}

// Функция для получения задач по ID проекта
async function fetchTasksByProjectId(projectId) {
    const response = await fetch(`http://localhost:8080/task-manager-api/tasks/by-project-id/${projectId}`);
    if (!response.ok) {
        throw new Error("Ошибка при загрузке задач");
    }
    return response.json();
}

// Функция для отображения задач
function displayTasks(tasks) {
    const tasksList = document.getElementById('tasks-list');
    tasksList.innerHTML = '';

    if (!tasks || tasks.length === 0) {
        tasksList.innerHTML = `
            <div class="empty-tasks">
                <i class="fas fa-tasks"></i>
                <p>Нет задач в этом проекте</p>
            </div>
        `;
        return;
    }

    tasks.forEach(task => {
        const taskElement = document.createElement('div');
        taskElement.classList.add('task-card');
        taskElement.setAttribute('data-task-id', task.id);

        // Форматирование дат
        const deadline = formatDate(task.deadline);
        const startDate = formatDate(task.startDate);
        const updateDate = formatDate(task.updateDate);

        taskElement.innerHTML = `
            <h3 onclick="window.location.href='/task-page/${task.id}'">${task.title || 'Без названия'}</h3>
            <div class="task-meta">
                <span class="status ${task.status?.id === 5 ? 'status-overdue' : ''}">${task.status?.status || 'Не указан'}</span>
                <span class="priority">Приоритет: ${task.priority || 'Не указан'}</span>
                <span class="deadline">${deadline}</span>
            </div>
            <div class="task-details">
                <p><strong>Начало:</strong> ${startDate}</p>
                <p><strong>Обновлено:</strong> ${updateDate}</p>
                <p><strong>Категория:</strong> ${task.category?.name || 'Не указана'}</p>
                <p><strong>Исполнитель:</strong> ${task.assignee ? `${task.assignee.name} ${task.assignee.surname}` : 'Не назначен'}</p>
            </div>
            <div class="task-actions">
                <button class="btn-edit"><i class="fas fa-edit"></i> Редактировать</button>
                <button class="btn-delete"><i class="fas fa-trash"></i> Удалить</button>
            </div>
        `;

        tasksList.appendChild(taskElement);
    });

    // Назначаем обработчики для кнопок редактирования и удаления
    setupTaskActions();
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

// Настройка обработчиков для кнопок задач
function setupTaskActions() {
    // Обработчик для кнопки "Редактировать"
    document.querySelectorAll('.btn-edit').forEach(button => {
        button.addEventListener('click', function(event) {
            event.stopPropagation();
            const taskId = this.closest('.task-card').getAttribute('data-task-id');
            if (taskId) {
                window.location.href = `/edit-task?id=${taskId}`;
            } else {
                showError('Ошибка: ID задачи не найден');
            }
        });
    });

    // Обработчик для кнопки "Удалить"
    document.querySelectorAll('.btn-delete').forEach(button => {
        button.addEventListener('click', function(event) {
            event.stopPropagation();
            const taskElement = this.closest('.task-card');
            const taskId = taskElement.getAttribute('data-task-id');

            if (taskId) {
                confirmDeleteTask(taskId, taskElement);
            } else {
                showError('Ошибка: ID задачи не найден');
            }
        });
    });
}

// Подтверждение удаления задачи
function confirmDeleteTask(taskId, taskElement) {
    Swal.fire({
        icon: 'warning',
        title: 'Вы уверены?',
        text: 'Вы действительно хотите удалить эту задачу?',
        showCancelButton: true,
        confirmButtonText: 'Да, удалить',
        cancelButtonText: 'Отмена',
        confirmButtonColor: '#dc3545'
    }).then((result) => {
        if (result.isConfirmed) {
            deleteTask(taskId, taskElement);
        }
    });
}

// Удаление задачи
async function deleteTask(taskId, taskElement) {
    try {
        const response = await fetch(`http://localhost:8080/task-manager-api/tasks/${taskId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            taskElement.remove();
            showSuccess('Задача успешно удалена');

            // Проверяем, остались ли еще задачи
            const tasksList = document.getElementById('tasks-list');
            if (tasksList.children.length === 0) {
                tasksList.innerHTML = `
                    <div class="empty-tasks">
                        <i class="fas fa-tasks"></i>
                        <p>Нет задач в этом проекте</p>
                    </div>
                `;
            }
        } else {
            throw new Error('Ошибка сервера');
        }
    } catch (error) {
        console.error('Ошибка при удалении задачи:', error);
        showError('Не удалось удалить задачу');
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