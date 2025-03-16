document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("create-task-form");
    const categorySelect = document.getElementById("category");
    const assigneeSelect = document.getElementById("assignee");
    const projectSelect = document.getElementById("project");

    // Загружаем данные для формы
    Promise.all([fetchCategories(), fetchUsers(), fetchProjects()])
        .then(([categories, users, projects]) => {
            fillSelect(categorySelect, categories, "name"); // Заполняем категории
            fillSelect(assigneeSelect, users, "name"); // Заполняем исполнителей
            fillSelect(projectSelect, projects, "name"); // Заполняем проекты
        })
        .catch(error => {
            console.error("Ошибка при загрузке данных:", error);
            showError("Не удалось загрузить данные для формы.");
        });

    // Обработчик отправки формы
    form.addEventListener("submit", function (event) {
        event.preventDefault();
        createTask(getFormData());
    });

    // Функция для загрузки категорий
    async function fetchCategories() {
        const response = await fetch("http://localhost:8082/task-manager-api/tasks/categories");
        if (!response.ok) {
            throw new Error("Ошибка при загрузке категорий");
        }
        return response.json();
    }

    // Функция для загрузки пользователей
    async function fetchUsers() {
        const response = await fetch("http://localhost:8082/task-manager-api/users");
        if (!response.ok) {
            throw new Error("Ошибка при загрузке пользователей");
        }
        return response.json();
    }

    // Функция для загрузки проектов
    async function fetchProjects() {
        const response = await fetch("http://localhost:8082/task-manager-api/projects");
        if (!response.ok) {
            throw new Error("Ошибка при загрузке проектов");
        }
        return response.json();
    }

    // Функция для заполнения выпадающего списка
    function fillSelect(select, data, displayField) {
        data.forEach(item => {
            const option = document.createElement("option");
            option.value = item.id;
            option.textContent = item[displayField];
            select.appendChild(option);
        });
    }

    // Функция для получения данных из формы
    function getFormData() {
        return {
            title: document.getElementById("title").value,
            description: document.getElementById("description").value,
            priority: parseInt(document.getElementById("priority").value),
            deadline: document.getElementById("deadline").value,
            categoryId: parseInt(document.getElementById("category").value),
            assigneeId: parseInt(document.getElementById("assignee").value),
            projectId: parseInt(document.getElementById("project").value)
        };
    }

    // Функция для создания задачи
    async function createTask(data) {
        try {
            const response = await fetch("http://localhost:8082/task-manager-api/tasks", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                throw new Error("Ошибка при создании задачи");
            }

            showSuccess("Задача успешно создана!", () => {
                window.location.href = "index"; // Перенаправляем на главную страницу
            });
        } catch (error) {
            console.error("Ошибка:", error);
            showError("Не удалось создать задачу.");
        }
    }

    // Функция для отображения успешного уведомления
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

    // Функция для отображения уведомления об ошибке
    function showError(message) {
        Swal.fire({
            icon: 'error',
            title: 'Ошибка',
            text: message,
            confirmButtonText: 'ОК'
        });
    }
});