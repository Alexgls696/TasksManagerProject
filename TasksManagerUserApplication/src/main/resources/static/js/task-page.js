// Получаем ID задачи из URL
const taskId = window.location.pathname.split('/').pop();

// Функция для форматирования даты
function formatDate(dateString) {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('ru-RU', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
    }).format(date);
}

// Функция для получения данных проекта по ID
async function fetchProject(projectId) {
    try {
        const response = await fetch(`http://localhost:8080/task-manager-api/projects/${projectId}`);
        if (!response.ok) {
            throw new Error('Ошибка при получении данных проекта');
        }
        const project = await response.json();
        return project.name; // Возвращаем название проекта
    } catch (error) {
        console.error('Ошибка:', error);
        return 'Неизвестный проект'; // Возвращаем значение по умолчанию в случае ошибки
    }
}

// Функция для получения данных задачи
async function fetchTaskDetails(taskId) {
    try {
        const response = await fetch(`http://localhost:8080/task-manager-api/tasks/${taskId}`);
        if (!response.ok) {
            throw new Error('Ошибка при получении данных задачи');
        }
        const task = await response.json();

        // Заполняем поля на странице
        document.getElementById('back-to-task-list').href=`http://localhost:8080/tasks?projectId=${task.projectId}`
        document.getElementById('task-id').textContent = task.id;
        document.getElementById('task-title').textContent = task.title;
        document.getElementById('task-description').textContent = task.description;
        document.getElementById('task-status').textContent = task.status?.status || 'Не указано';
        document.getElementById('task-priority').textContent = task.priority;
        document.getElementById('task-deadline').textContent = formatDate(task.deadline);
        document.getElementById('task-startDate').textContent = formatDate(task.startDate);
        document.getElementById('task-updateDate').textContent = formatDate(task.updateDate);
        document.getElementById('task-category').textContent = task.category?.name || 'Не указано';
        document.getElementById('task-assignee').textContent = task.assignee ? `${task.assignee.name} ${task.assignee.surname}` : 'Не назначено';
        document.getElementById('task-creator').textContent = task.creator ? `${task.creator.name} ${task.creator.surname}` : 'Не указано';

        // Получаем и отображаем название проекта
        const projectName = await fetchProject(task.projectId);
        document.getElementById('task-project').textContent = projectName;

    } catch (error) {
        console.error('Ошибка:', error);
        alert('Не удалось загрузить данные задачи. Пожалуйста, попробуйте позже.');
    }
}

// Функция для добавления заметки
function addNote() {
    const noteText = document.getElementById('new-note').value.trim();
    if (noteText) {
        const notesGrid = document.getElementById('notes-grid');
        const noteElement = document.createElement('div');
        noteElement.classList.add('note');
        noteElement.innerHTML = `<p>${noteText}</p>`;
        notesGrid.appendChild(noteElement);

        // Очищаем текстовое поле
        document.getElementById('new-note').value = '';
    }
}

// Обработчик для кнопки добавления заметки
document.getElementById('add-note-btn').addEventListener('click', addNote);

// Вызываем функцию для получения данных задачи
fetchTaskDetails(taskId);