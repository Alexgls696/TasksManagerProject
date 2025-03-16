document.addEventListener("DOMContentLoaded", function () {
    const taskList = document.querySelector(".task-list");

    // Получаем ID проекта из URL
    const urlParams = new URLSearchParams(window.location.search);
    const projectId = urlParams.get("projectId");

    if (!projectId) {
        alert("ID проекта не указан.");
        window.location.href = "/index"; // Перенаправляем на главную страницу
        return;
    }
    document.getElementById('project_id_input').value = projectId;

    // Загружаем задачи для проекта
    fetchTasksByProjectId(projectId)
        .then(tasks => {
            if(tasks.length > 0) {
                displayTasks(tasks);
            }
        })
        .catch(error => {
            console.error("Ошибка при загрузке задач:", error);
            alert("Не удалось загрузить задачи.");
        });

    // Функция для загрузки задач по ID проекта
    async function fetchTasksByProjectId(projectId) {
        const response = await fetch(`http://localhost:8080/task-manager-api/tasks/by-project-id/${projectId}`);
        if (!response.ok) {
            throw new Error("Ошибка при загрузке задач");
        }
        return response.json();
    }

    let projectNameToModel = null;
    // Функция для отображения задач
    function displayTasks(tasks) {
        taskList.innerHTML = ""; // Очищаем список задач
        tasks.forEach(task => {
            const taskElement = document.createElement("div");
            taskElement.classList.add("task");
            taskElement.setAttribute("data-task-id", task.id); // Добавляем data-task-id

            // Форматирование дат
            const deadline = formatDate(task.deadline);
            const startDate = formatDate(task.startDate);
            const updateDate = formatDate(task.updateDate);

            // Создание HTML для задачи
            taskElement.innerHTML = `
            <h3>${task.title}</h3>
            <div class="task-meta">
                <span class="status">Статус: ${task.status.status}</span>
                <span class="priority">Приоритет: ${task.priority}</span>
                <span class="deadline">Дедлайн: ${deadline}</span>
            </div>
            <div class="task-details">
                <p><strong>Начало:</strong> ${startDate}</p>
                <p><strong>Обновлено:</strong> ${updateDate}</p>
                <p><strong>Категория:</strong> ${task.category ? task.category.name : "Нет"}</p>
                <p><strong>Исполнитель:</strong> ${task.assignee ? task.assignee.name : "Не назначен"}</p>
                <p><strong>Создатель:</strong> ${task.creator.name}</p>
                <p><strong>Проект:</strong> ${task.project ? task.project.name : "Нет"}</p>
            </div>
            <button class="btn-edit">Редактировать</button>
            <button class="btn-delete">Удалить</button>
        `;

            taskList.appendChild(taskElement);
        });
    }
    // Функция для форматирования даты
    function formatDate(dateString) {
        if (!dateString) return "Нет данных";
        const date = new Date(dateString);
        return date.toLocaleString("ru-RU", {
            year: "numeric",
            month: "long",
            day: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        });
    }

    // Обработчик для кнопки "Редактировать"
    taskList.addEventListener("click", function (event) {
        if (event.target.classList.contains("btn-edit")) {
            const task = event.target.closest(".task");
            const taskId = task.dataset.taskId; // Получаем ID задачи из data-атрибута
            if (taskId) {
                window.location.href = `/edit?id=${taskId}`; // Перенаправляем на страницу редактирования
            } else {
                alert("Ошибка: ID задачи не найден.");
            }
        }
    });

    // Обработчик для кнопки "Удалить"
    taskList.addEventListener("click", function (event) {
        if (event.target.classList.contains("btn-delete")) {
            const taskElement = event.target.closest(".task");
            const taskId = taskElement.getAttribute("data-task-id");

            // Подтверждение удаления
            Swal.fire({
                icon: 'warning',
                title: 'Вы уверены?',
                text: 'Вы действительно хотите удалить эту задачу?',
                showCancelButton: true,
                confirmButtonText: 'Да, удалить',
                cancelButtonText: 'Отмена'
            }).then((result) => {
                if (result.isConfirmed) {
                    // Отправка DELETE-запроса на сервер
                    fetch(`http://localhost:8080/task-manager-api/tasks/${taskId}`, {
                        method: 'DELETE'
                    })
                        .then(response => {
                            if (response.ok) {
                                // Удаление задачи из DOM
                                taskElement.remove();
                                showSuccess("Задача успешно удалена!");
                            } else {
                                showError("Ошибка при удалении задачи.");
                            }
                        })
                        .catch(error => {
                            showError("Ошибка при удалении задачи.");
                        });
                }
            });
        }
    });

// Функция для показа успешного уведомления
    function showSuccess(message, callback) {
        Swal.fire({
            icon: 'success',
            title: 'Успех!',
            text: message,
            confirmButtonText: 'ОК'
        }).then((result) => {
            if (result.isConfirmed && callback) {
                callback();
            }
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

    document.getElementById('add-task-link').addEventListener('click', function(event) {
        event.preventDefault(); // Отменяем стандартное поведение ссылки
        document.getElementById('create-task-form').submit(); // Отправляем форму
    });

    let project = getProject()
        .then(_project=>{
            project = _project;
        })

    async function getProject(){
        const response = await fetch(`http://localhost:8080/task-manager-api/projects/${projectId}`);
        if (!response.ok) {
            throw new Error("Ошибка при загрузке задач");
        }
        return response.json();
    }

    (async ()=>{
        let project = await getProject();
        document.getElementById('project_name_input').value = project.name;
    })();
});
