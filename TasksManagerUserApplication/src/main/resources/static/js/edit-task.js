document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("edit-task-form");
    const assigneeSelect = document.getElementById("assignee");
    const statusSelect = document.getElementById("status");
    // Получаем ID задачи из URL (например, /edit-task.html?id=1)
    const urlParams = new URLSearchParams(window.location.search);
    const taskId = urlParams.get("id");

    if (!taskId) {
        alert("ID задачи не указан");
        window.location.href = "/"; // Перенаправляем на главную страницу
        return;
    }

    // Загружаем данные задачи и список пользователей

    let _users
    Promise.all([fetchTask(taskId), fetchUsers()])
        .then(([task, users]) => {
            fillAssigneeSelect(users);
            fillForm(task);
            _users = users;
        })
        .catch(error => {
            console.error("Ошибка:", error);
            alert("Не удалось загрузить данные для редактирования.");
        });

    // Обработчик отправки формы
    form.addEventListener("submit", function (event) {
        event.preventDefault();
        updateTask(taskId, getFormData());
    });



    // Функция для загрузки задачи
    async function fetchTask(taskId) {
        const response = await fetch(`http://localhost:8082/task-manager-api/tasks/${taskId}`);
        if (!response.ok) {
            throw new Error("Ошибка при загрузке задачи");
        }
        return response.json();
    }

    // Функция для загрузки списка пользователей
    async function fetchUsers() {
        const response = await fetch("http://localhost:8082/task-manager-api/users");
        if (!response.ok) {
            throw new Error("Ошибка при загрузке пользователей");
        }
        return response.json();
    }

    async function fetchStatuses(){
        const response = await fetch('http://localhost:8082/task-manager-api/tasks/statuses');
        if(!response.ok){
            throw new Error("Ошибка при загрузке статусов задачи");
        }
        return response.json();
    }

    function fillStatusSelect(statuses) {
        statusSelect.innerHTML = "";

        statuses.forEach(status => {
            const option = document.createElement("option");
            option.value = status.status;
            option.textContent = status.status;
            statusSelect.appendChild(option);
        });
    }

    let _statues ;
    fetchStatuses()
        .then(statuses => {
            fillStatusSelect(statuses); // Заполняем выпадающий список статусами
            _statues = statuses;
        })
        .catch(error => {
            console.error("Ошибка при загрузке статусов:", error);
            alert("Не удалось загрузить статусы задач.");
        });

    // Функция для заполнения формы данными задачи
    function fillForm(task) {
        document.getElementById("title").value = task.title;
        document.getElementById("description").value = task.description;
        document.getElementById("status").value = task.status.status;
        document.getElementById("priority").value = task.priority;
        document.getElementById("deadline").value = formatDateTimeLocal(task.deadline);
        let children  =  document.getElementById("assignee").children;
        document.getElementById("assignee").value = task.assignee.id;
    }

    // Функция для заполнения списка исполнителей
    function fillAssigneeSelect(users) {
        users.forEach(user => {
            const option = document.createElement("option");
            option.value = user.id;
            option.textContent = user.name;
            assigneeSelect.appendChild(option);
        });
    }

    // Функция для получения данных из формы
    function getFormData() {
        let _user = null;
        let _status = null;

        for(let i = 0; i < _statues.length; i++){
            if (_statues[i].status==document.getElementById("status").value){
                _status = _statues[i].id; break;
            }
        }

        let userId = parseInt(document.getElementById("assignee").value);
        for(let i = 0; i < _users.length; i++){
            if (_users[i].id==userId){
                _user = _users[i].id; break;
            }
        }

        return {
            title: document.getElementById("title").value,
            description: document.getElementById("description").value,
            priority: parseInt(document.getElementById("priority").value),
            deadline: document.getElementById("deadline").value,
            statusId: _status,
            assigneeId: _user
        };
    }

    async function updateTask(taskId, data) {
        try {
            const response = await fetch(`http://localhost:8082/task-manager-api/tasks/${taskId}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                throw new Error("Ошибка при обновлении задачи");
            }

            // Успешное уведомление
            Swal.fire({
                icon: 'success',
                title: 'Успех!',
                text: 'Задача успешно обновлена!',
                confirmButtonText: 'ОК'
            }).then(() => {
                //window.location.href = "/"; // Перенаправление после закрытия уведомления
            });
        } catch (error) {
            console.error("Ошибка:", error);

            // Уведомление об ошибке
            Swal.fire({
                icon: 'error',
                title: 'Ошибка',
                text: 'Не удалось обновить задачу.',
                confirmButtonText: 'ОК'
            });
        }
    }

    // Функция для форматирования даты и времени в формат datetime-local
    function formatDateTimeLocal(dateString) {
        const date = new Date(dateString);
        return date.toISOString().slice(0, 16); // Формат: YYYY-MM-DDTHH:MM
    }
});