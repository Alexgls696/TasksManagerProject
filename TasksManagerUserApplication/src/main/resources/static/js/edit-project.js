document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("edit-project-form");
    const statusSelect = document.getElementById("status");
    const membersSelect = document.getElementById("members");

    // Получаем ID проекта из URL
    const urlParams = new URLSearchParams(window.location.search);
    const projectId = urlParams.get("id");

    if (!projectId) {
        alert("ID проекта не указан");
        window.location.href = "/"; // Перенаправляем на главную страницу
        return;
    }

    // Загружаем данные проекта, статусы и пользователей
    let _statuses, _users;
    Promise.all([fetchProject(projectId), fetchStatuses(), fetchUsers()])
        .then(([project, statuses, users]) => {
            fillStatusSelect(statuses);
            fillMembersSelect(users);
            fillForm(project);
            _statuses = statuses;
            _users = users;
        })
        .catch(error => {
            console.error("Ошибка:", error);
            alert("Не удалось загрузить данные для редактирования.");
        });

    // Обработчик отправки формы
    form.addEventListener("submit", function (event) {
        event.preventDefault();
        updateProject(projectId, getFormData());
    });

    // Функция для загрузки проекта
    async function fetchProject(projectId) {
        const response = await fetch(`http://localhost:8080/task-manager-api/projects/${projectId}`);
        if (!response.ok) {
            throw new Error("Ошибка при загрузке проекта");
        }
        return response.json();
    }

    // Функция для загрузки списка статусов
    async function fetchStatuses() {
        const response = await fetch("http://localhost:8080/task-manager-api/projects/statuses");
        if (!response.ok) {
            throw new Error("Ошибка при загрузке статусов");
        }
        return response.json();
    }

    // Функция для загрузки списка пользователей
    async function fetchUsers() {
        const response = await fetch("http://localhost:8080/task-manager-api/users");
        if (!response.ok) {
            throw new Error("Ошибка при загрузке пользователей");
        }
        return response.json();
    }

    // Функция для заполнения списка статусов
    function fillStatusSelect(statuses) {
        statusSelect.innerHTML = "";
        statuses.forEach(status => {
            const option = document.createElement("option");
            option.value = status.id;
            option.textContent = status.status;
            statusSelect.appendChild(option);
        });
    }

    // Функция для заполнения списка участников
    function fillMembersSelect(users) {
        membersSelect.innerHTML = "";
        users.forEach(user => {
            const option = document.createElement("option");
            option.value = user.id;
            option.textContent = user.name;
            membersSelect.appendChild(option);
        });
    }

    // Функция для заполнения формы данными проекта
    function fillForm(project) {
        document.getElementById("name").value = project.name;
        document.getElementById("description").value = project.description;
        document.getElementById("deadline").value = formatDateTimeLocal(project.deadline);
        document.getElementById("status").value = project.projectStatus.id;

        // Выбираем участников проекта
        if (project.memberIds && project.memberIds.length > 0) {
            Array.from(membersSelect.options).forEach(option => {
                if (project.memberIds.includes(parseInt(option.value))) {
                    option.selected = true;
                }
            });
        }
    }

    // Функция для получения данных из формы
    function getFormData() {
        const selectedMembers = Array.from(membersSelect.selectedOptions).map(option => parseInt(option.value));
        let _status = document.getElementById("status");
        return {
            name: document.getElementById("name").value,
            description: document.getElementById("description").value,
            deadline: document.getElementById("deadline").value,
            status: {
                id: parseInt(document.getElementById("status").value),
                status: _status.options[_status.selectedIndex].text
            },
            membersId: selectedMembers // Преобразуем массив в Set
        };
    }

    // Функция для обновления проекта
    async function updateProject(projectId, data) {
        try {
            console.log(data);
            const response = await fetch(`http://localhost:8080/task-manager-api/projects/${projectId}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                throw new Error("Ошибка при обновлении проекта");
            }

            // Успешное уведомление
            Swal.fire({
                icon: 'success',
                title: 'Успех!',
                text: 'Проект успешно обновлен!',
                confirmButtonText: 'ОК'
            }).then(() => {
                window.location.href = "/"; // Перенаправление после закрытия уведомления
            });
        } catch (error) {
            console.error("Ошибка:", error);

            // Уведомление об ошибке
            Swal.fire({
                icon: 'error',
                title: 'Ошибка',
                text: 'Не удалось обновить проект.',
                confirmButtonText: 'ОК'
            });
        }
    }

    // Функция для форматирования даты и времени в формат datetime-local
    function formatDateTimeLocal(dateString) {
        if (!dateString) return "";
        const date = new Date(dateString);
        return date.toISOString().slice(0, 16); // Формат: YYYY-MM-DDTHH:MM
    }
});