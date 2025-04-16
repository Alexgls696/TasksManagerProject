import { fetchWithAuth } from './auth_utils.js';
//-----------------------------------------------------------------------------------------------------------------------

let access = false;
document.addEventListener("DOMContentLoaded", function () {
    // Загружаем проекты

    // Функция для загрузки проектов
    async function loadProjects() {
        try {
            const projects = await fetchProjects();
            displayProjects(projects);
        } catch (error) {
            console.error("Ошибка при загрузке проектов:", error);
            showError("Не удалось загрузить проекты");
        }
    }

    // Функция для получения проектов
    async function fetchProjects() {
        const response = await fetchWithAuth("/task-manager-api/projects/by-user"); //await fetch("http://localhost:8080/task-manager-api/projects");
        if (!response.ok) {
            throw new Error("Ошибка при загрузке проектов");
        }
        return response.json();
    }

    // Функция для отображения проектов
    function displayProjects(projects) {
        const projectsGrid = document.getElementById('projects-grid');
        projectsGrid.innerHTML = '';

        if (!projects || projects.length === 0) {
            projectsGrid.innerHTML = `
                <div class="empty-projects">
                    <i class="fas fa-project-diagram"></i>
                    <p>У вас пока нет проектов</p>
                    <a id="create-project-big-button" href="/create-project" class="btn-primary"><i class="fas fa-plus"></i> Создать первый проект</a>
                </div>
            `;
            return;
        }

        projects.forEach(project => {
            const projectElement = document.createElement('div');
            projectElement.classList.add('project-card');
            projectElement.setAttribute('data-project-id', project.id);

            // Форматирование дат
            const creationDate = formatDate(project.creationDate);
            const deadline = formatDate(project.deadline);

            // Определяем класс статуса
            let statusClass = 'project-status';
            if (project.projectStatus) {
                const status = project.projectStatus.status.toLowerCase();
                if (status.includes('актив')) statusClass += ' status-active';
                else if (status.includes('заверш')) statusClass += ' status-completed';
                else if (status.includes('архив')) statusClass += ' status-archived';
            }

            projectElement.innerHTML = `
                <div class="${statusClass}">${project.projectStatus?.status || 'Без статуса'}</div>
                <h3><a href="/tasks?projectId=${project.id}">${project.name || 'Без названия'}</a></h3>
                <div class="project-meta">
                    <p><strong><i class="fas fa-align-left"></i> Описание:</strong> ${project.description || 'Нет описания'}</p>
                    <p><strong><i class="fas fa-calendar-times"></i> Дедлайн:</strong> ${deadline}</p>
                    <p><strong><i class="fas fa-user-edit"></i> Создатель:</strong> ${project.creator?.name || 'Неизвестно'} ${project.creator?.surname || ''}</p>
                </div>
                <div class="project-actions">
                    <button class="btn-edit"><i class="fas fa-edit"></i> Редактировать</button>
                    <button class="btn-delete"><i class="fas fa-trash"></i> Удалить</button>
                </div>
            `;

            projectsGrid.appendChild(projectElement);
        });

        // Назначаем обработчики для кнопок
        setupProjectActions();
    }

    async function getUserRoles(){
        const response = await fetchWithAuth('/task-manager-api/users/current-user-roles');
        if(!response.ok){
            console.error('Ошибка выполнения запроса пользователя');
        }
        if(response.status===401){
            console.log('Ошибка, вы не авторизованы');
            access = false;
            return null;
        }
        if (response.status===403){
            console.log('У вас нет прав для выполнения данной операции');
            access = false;
            return null;
        }
        return await response.json();
    }

    function checkPermission(roles){
        for(let i = 0; i < roles.length; i++){
            if(roles[i]==='ROLE_MANAGER'){
                return true;
            }
        }
        return false;
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

    // Настройка обработчиков для кнопок проектов
    function setupProjectActions() {
        // Обработчик для кнопки "Редактировать"
        document.querySelectorAll('.btn-edit').forEach(button => {
            button.addEventListener('click', function(event) {
                event.stopPropagation();
                const projectId = this.closest('.project-card').getAttribute('data-project-id');
                if (projectId) {
                    window.location.href = `/edit-project?id=${projectId}`;
                } else {
                    showError('Ошибка: ID проекта не найден');
                }
            });
        });

        // Обработчик для кнопки "Удалить"
        document.querySelectorAll('.btn-delete').forEach(button => {
            button.addEventListener('click', function(event) {
                event.stopPropagation();
                const projectElement = this.closest('.project-card');
                const projectId = projectElement.getAttribute('data-project-id');

                if (projectId) {
                    confirmDeleteProject(projectId, projectElement);
                } else {
                    showError('Ошибка: ID проекта не найден');
                }
            });
        });
    }

    // Подтверждение удаления проекта
    function confirmDeleteProject(projectId, projectElement) {
        Swal.fire({
            icon: 'warning',
            title: 'Вы уверены?',
            text: 'Вы действительно хотите удалить этот проект? Все связанные задачи также будут удалены!',
            showCancelButton: true,
            confirmButtonText: 'Да, удалить',
            cancelButtonText: 'Отмена',
            confirmButtonColor: '#dc3545'
        }).then((result) => {
            if (result.isConfirmed) {
                deleteProject(projectId, projectElement);
            }
        });
    }

    // Удаление проекта
    async function deleteProject(projectId, projectElement) {
        try {
            const response = await fetchWithAuth(`/task-manager-api/projects/${projectId}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                projectElement.remove();
                showSuccess('Проект успешно удален');

                // Проверяем, остались ли еще проекты
                const projectsGrid = document.getElementById('projects-grid');
                if (projectsGrid.children.length === 0) {
                    projectsGrid.innerHTML = `
                        <div class="empty-projects">
                            <i class="fas fa-project-diagram"></i>
                            <p>У вас пока нет проектов</p>
                            <a href="/create-project" class="btn-primary"><i class="fas fa-plus"></i> Создать первый проект</a>
                        </div>
                    `;
                }
            } else {
                throw new Error('Ошибка сервера');
            }
        } catch (error) {
            console.error('Ошибка при удалении проекта:', error);
            showError('Не удалось удалить проект');
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


    function hideHiddenElements(){
        document.getElementById('create-project-button').style.display = 'none';
        document.getElementById('create-project-big-button').style.display = 'none';

        const editButtons = document.querySelectorAll('.btn-edit');
        const removeButtons = document.querySelectorAll('.btn-delete');

        editButtons.forEach(button=>{button.style.display='none'});
        removeButtons.forEach(button=>{button.style.display='none'});
    }

    (async ()=>{
        await loadProjects();
        const userInfo = await getUserRoles();
        const permission = checkPermission(userInfo.roles);
        if(!permission) hideHiddenElements();
    })();
});