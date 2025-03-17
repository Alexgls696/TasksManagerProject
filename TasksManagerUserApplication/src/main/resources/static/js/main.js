document.addEventListener("DOMContentLoaded", function () {
    const projectList = document.querySelector(".project-list");

    // Загружаем проекты
    fetchProjects()
        .then(projects => {
            if (projects.length > 0) {
                displayProjects(projects);
            } else {
                projectList.innerHTML = "<p>Проектов пока нет.</p>";
            }
        })
        .catch(error => {
            console.error("Ошибка при загрузке проектов:", error);
            alert("Не удалось загрузить проекты.");
        });

    // Функция для загрузки проектов
    async function fetchProjects() {
        const response = await fetch("http://localhost:8080/task-manager-api/projects");
        if (!response.ok) {
            throw new Error("Ошибка при загрузке проектов");
        }
        return response.json();
    }

    // Функция для отображения проектов
    function displayProjects(projects,creator) {
        projectList.innerHTML = ""; // Очищаем список проектов
        projects.forEach(project => {
            const projectElement = document.createElement("div");
            projectElement.classList.add("project");
            projectElement.setAttribute("data-project-id", project.id); // Добавляем data-project-id

            // Форматирование дат
            const creationDate = formatDate(project.creationDate);
            const deadline = formatDate(project.deadline);

            // Создание HTML для проекта
            projectElement.innerHTML = `
                <h3 id="project-name"><a href="tasks?projectId=${project.id}">${project.name}</a></h3>
                <div class="project-meta">
                    <p><strong>Описание:</strong> ${project.description || "Нет описания"}</p>
                    <p><strong>Статус:</strong> ${project.projectStatus ? project.projectStatus.status : "Нет данных"}</p>
                    <p><strong>Дедлайн:</strong> ${deadline}</p>
                </div>
                <div class="project-details">
                    <p><strong>Дата создания:</strong> ${creationDate}</p>
                    <p><strong>Создатель:</strong> ${project.creator.name} ${project.creator.surname}</p>
                </div>
                <div class="project-actions">
                    <button class="btn-edit">Редактировать</button>
                    <button class="btn-delete">Удалить</button>
                </div>
            `;

            projectList.appendChild(projectElement);
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
    projectList.addEventListener("click", function (event) {
        if (event.target.classList.contains("btn-edit")) {
            const project = event.target.closest(".project");
            const projectId = project.dataset.projectId; // Получаем ID проекта из data-атрибута
            if (projectId) {
                window.location.href = `/edit-project?id=${projectId}`; // Перенаправляем на страницу редактирования
            } else {
                alert("Ошибка: ID проекта не найден.");
            }
        }
    });

    // Обработчик для кнопки "Удалить"
    projectList.addEventListener("click", function (event) {
        if (event.target.classList.contains("btn-delete")) {
            const projectElement = event.target.closest(".project");
            const projectId = projectElement.getAttribute("data-project-id");

            // Подтверждение удаления
            Swal.fire({
                icon: 'warning',
                title: 'Вы уверены?',
                text: 'Вы действительно хотите удалить этот проект?',
                showCancelButton: true,
                confirmButtonText: 'Да, удалить',
                cancelButtonText: 'Отмена'
            }).then((result) => {
                if (result.isConfirmed) {
                    // Отправка DELETE-запроса на сервер
                    fetch(`http://localhost:8080/task-manager-api/projects/${projectId}`, {
                        method: 'DELETE'
                    })
                        .then(response => {
                            if (response.ok) {
                                // Удаление проекта из DOM
                                projectElement.remove();
                                showSuccess("Проект успешно удален!");
                            } else {
                                showError("Ошибка при удалении проекта.");
                            }
                        })
                        .catch(error => {
                            showError("Ошибка при удалении проекта.");
                        });
                }
            });
        }
    });

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