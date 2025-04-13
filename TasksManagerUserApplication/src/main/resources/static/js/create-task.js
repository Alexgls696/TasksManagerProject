import { fetchWithAuth } from './auth_utils.js';

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("create-task-form");
    const categorySelect = document.getElementById("category");
    const assigneeSelect = document.getElementById("assignee");
    const projectSelect = document.getElementById("project");
    const membersSelect = document.getElementById("members");

    // Загружаем данные для формы
    Promise.all([fetchCategories(), fetchUsers()])
        .then(([categories, users]) => {
            fillSelect(categorySelect, categories, "name"); // Заполняем категории
            fillSelect(assigneeSelect, users, "name"); // Заполняем исполнителей
            fillSelect(membersSelect, users, "name", true); // Заполняем участников (множественный выбор)
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
        const response = await fetchWithAuth("/task-manager-api/tasks/categories");
        if (!response.ok) {
            throw new Error("Ошибка при загрузке категорий");
        }
        return response.json();
    }

    // Функция для загрузки пользователей
    async function fetchUsers() {
        const response = await fetchWithAuth("http://localhost:8080/task-manager-api/users");
        if (!response.ok) {
            throw new Error("Ошибка при загрузке пользователей");
        }
        return response.json();
    }

    // Функция для заполнения выпадающего списка
    function fillSelect(select, data, displayField, isMultiple = false) {
        if (!select) {
            console.error(`Элемент select не найден: ${select?.id}`);
            return;
        }

        // Очищаем существующие опции
        select.innerHTML = '';

        // Для не-множественного выбора добавляем пустую опцию
        if (!isMultiple) {
            const defaultOption = document.createElement("option");
            defaultOption.value = "";
            defaultOption.textContent = "-- Выберите --";
            defaultOption.disabled = true;
            defaultOption.selected = true;
            select.appendChild(defaultOption);
        }

        data.forEach(item => {
            const option = document.createElement("option");
            option.value = item.id;
            option.textContent = item[displayField];
            select.appendChild(option);
        });
    }

    // Функция для получения данных из формы
    function getFormData() {
        // Получаем выбранных участников
        const membersOptions = Array.from(document.getElementById("members").selectedOptions);
        const members = membersOptions.map(option => parseInt(option.value));

        return {
            title: document.getElementById("title").value,
            description: document.getElementById("description").value,
            priority: parseInt(document.getElementById("priority").value),
            deadline: document.getElementById("deadline").value,
            categoryId: parseInt(document.getElementById("category").value),
            projectId: parseInt(document.getElementById("project").value),
            membersId: members
        };
    }

    // Функция для создания задачи
    async function createTask(data) {
        try {
            console.log("Отправляемые данные:", data);
            const response = await fetchWithAuth("/task-manager-api/tasks", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || "Ошибка при создании задачи");
            }

            showSuccess("Задача успешно создана!", () => {
                window.location.href = `/tasks?projectId=${data.projectId}`;
            });
        } catch (error) {
            console.error("Ошибка:", error);
            showError(error.message || "Не удалось создать задачу.");
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