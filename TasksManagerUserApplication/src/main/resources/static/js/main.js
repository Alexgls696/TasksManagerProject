document.addEventListener("DOMContentLoaded", function () {
    const projectList = document.querySelector(".project-list");

    // Загружаем проекты
    fetchProjects()
        .then(projects => {
            displayProjects(projects);
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
    function displayProjects(projects) {
        projectList.innerHTML = ""; // Очищаем список проектов

        projects.forEach(project => {
            const projectElement = document.createElement("div");
            projectElement.classList.add("project");

            // Создаем HTML для проекта
            projectElement.innerHTML = `
                <h3><a href="tasks?projectId=${project.id}">${project.name}</a></h3>
                <p>${project.description}</p>
            `;
            projectList.appendChild(projectElement);
        });
    }
});