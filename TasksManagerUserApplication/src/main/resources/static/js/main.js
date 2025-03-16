document.addEventListener("DOMContentLoaded", function () {
    const taskList = document.querySelector(".task-list");

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
            const task = event.target.closest(".task");
            task.remove();
            alert("Задача удалена!");
        }
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const taskList = document.querySelector(".task-list");

    // Функция для получения задач с API
    async function fetchTasks() {
        try {
            const response = await fetch("http://localhost:8082/task-manager-api/tasks");
            if (!response.ok) {
                throw new Error("Ошибка при загрузке задач");
            }
            const tasks = await response.json();
            displayTasks(tasks);
        } catch (error) {
            console.error("Ошибка:", error);
            alert("Не удалось загрузить задачи. Пожалуйста, попробуйте позже.");
        }
    }

    // Функция для отображения задач на странице
    function displayTasks(tasks) {
        taskList.innerHTML = ""; // Очистка списка задач

        tasks.forEach(task => {
            const taskElement = document.createElement("div");
            taskElement.classList.add("task");
            taskElement.setAttribute("data-task-id", task.id)

            // Форматирование дат
            const deadline = formatDate(task.deadline);
            const startDate = formatDate(task.startDate);
            const updateDate = formatDate(task.updateDate);

            // Создание HTML для задачи
            taskElement.innerHTML = `
                <h3>${task.title}</h3>
                <p>${task.description}</p>
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
            const taskTitle = task.querySelector("h3").textContent;
            alert(`Редактирование задачи: ${taskTitle}`);
        }
    });

    // Обработчик для кнопки "Удалить"
    taskList.addEventListener("click", function (event) {
        if (event.target.classList.contains("btn-delete")) {
            const task = event.target.closest(".task");
            task.remove();
            alert("Задача удалена!");
        }
    });

    // Загрузка задач при загрузке страницы
    fetchTasks();
});