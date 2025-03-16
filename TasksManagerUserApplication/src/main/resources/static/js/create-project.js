document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("create-project-form");
    const creatorSelect = document.getElementById("creator");
    const membersSelect = document.getElementById("members");

    // Загружаем данные для формы (пользователи)
    fetchUsers()
        .then(users => {
            fillSelect(creatorSelect, users, "name"); // Заполняем создателя
            fillSelect(membersSelect, users, "name"); // Заполняем участников
        })
        .catch(error => {
            console.error("Ошибка при загрузке данных:", error);
            showError("Не удалось загрузить данные для формы.");
        });

    // Обработчик отправки формы
    form.addEventListener("submit", function (event) {
        event.preventDefault();
        createProject(getFormData());
    });

    // Функция для загрузки пользователей
    async function fetchUsers() {
        const response = await fetch("http://localhost:8080/task-manager-api/users");
        if (!response.ok) {
            throw new Error("Ошибка при загрузке пользователей");
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
        const members = Array.from(document.getElementById("members").selectedOptions)
            .map(option => parseInt(option.value));
        console.log(members);
        return {
            name: document.getElementById("name").value,
            description: document.getElementById("description").value,
            deadline: document.getElementById("deadline").value,
            creatorId: parseInt(document.getElementById("creator").value),
            membersId: members
        };
    }

    // Функция для создания проекта
    async function createProject(data) {
        try {
            const response = await fetch("http://localhost:8080/task-manager-api/projects", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                throw new Error("Ошибка при создании проекта");
            }

            showSuccess("Проект успешно создан!", () => {
                window.location.href = "index"; // Перенаправляем на главную страницу
            });
        } catch (error) {
            console.error("Ошибка:", error);
            showError("Не удалось создать проект.");
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