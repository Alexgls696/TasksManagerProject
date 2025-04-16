import { fetchWithAuth } from './auth_utils.js';

// Получаем ID задачи из URL
const taskId = window.location.pathname.split('/').pop();

// Функция для форматирования даты
function formatDate(dateString) {
    if (!dateString) return 'Не указано';
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
    if (!projectId) return 'Не указан';
    try {
        const response = await fetchWithAuth(`/task-manager-api/projects/${projectId}`);
        if (!response.ok) {
            throw new Error('Ошибка при получении данных проекта');
        }
        const project = await response.json();
        return project.name || 'Неизвестный проект';
    } catch (error) {
        console.error('Ошибка:', error);
        return 'Неизвестный проект';
    }
}

// Функция для загрузки заметок задачи
async function fetchTaskNotes(taskId) {
    try {
        const response = await fetchWithAuth(`/task-manager-api/task-notes/by-task-id/${taskId}`);
        if (!response.ok) {
            throw new Error('Ошибка при получении заметок');
        }
        const notes = await response.json();
        return Array.isArray(notes) ? notes : [];
    } catch (error) {
        console.error('Ошибка загрузки заметок:', error);
        return [];
    }
}

// Функция для отображения заметок
// Функция для отображения заметок
async function displayNotes(notes) {
    const notesGrid = document.getElementById('notes-grid');
    if (!notesGrid) return;

    notesGrid.innerHTML = '';

    if (!notes || notes.length === 0) {
        notesGrid.innerHTML = `
            <div class="empty-notes">
                <i class="fas fa-comment-slash"></i>
                <p>Пока нет заметок для этой задачи</p>
            </div>
        `;
        return;
    }

    // Создаем массив промисов для загрузки данных создателей
    const notesWithCreators = await Promise.all(notes.map(async note => {
        try {
            // Загружаем данные создателя заметки
            const response = await fetchWithAuth(`/task-manager-api/users/${note.creatorId}/initials`);
            if (!response.ok) {
                throw new Error('Ошибка при загрузке данных пользователя');
            }
            const creatorName = await response.text();
            return { ...note, creatorName };
        } catch (error) {
            console.error('Ошибка загрузки данных создателя:', error);
            return { ...note, creatorName: 'Неизвестный автор' };
        }
    }));

    // Отображаем заметки с информацией о создателе
    notesWithCreators.forEach(note => {
        console.log(note);
        const noteElement = document.createElement('div');
        noteElement.classList.add('note');
        noteElement.innerHTML = `
            <div class="note-header">
                <h3>${note.title || 'Без названия'}</h3>
                <span class="note-creator">${note.creatorName}</span>
            </div>
            <p>${note.content || ''}</p>
            <div class="note-meta">
                <span>${formatDate(note.creationDate)}</span>
            </div>
        `;
        notesGrid.appendChild(noteElement);
    });
}

async function displayMembers(membersIds) {
    const membersList = document.getElementById('task-members');
    membersList.innerHTML = '';

    for (const memberId of membersIds) {
        try {
            const user = await fetchWithAuth(`/task-manager-api/users/${memberId}`);
            const data = await user.json();
            console.log(data);
            const memberItem = document.createElement('li');
            memberItem.textContent = `${data.username} (${data.name} ${data.surname})`;
            membersList.appendChild(memberItem);
        } catch (error) {
            console.error(`Ошибка при загрузке пользователя ${memberId}:`, error);
        }
    }
}

// Функция для получения данных задачи
async function fetchTaskDetails(taskId) {
    try {
        const response = await fetchWithAuth(`/task-manager-api/tasks/${taskId}`);
        if (!response.ok) {
            throw new Error('Ошибка при получении данных задачи');
        }
        const task = await response.json();
        console.log(task);

        await displayMembers(task.membersId);
        // Заполняем поля на странице
        const backLink = document.getElementById('back-to-task-list');
        if (backLink && task.projectId) {
            backLink.href = `/tasks?projectId=${task.projectId}`;
        }

        // Заголовок задачи
        const taskTitle = document.getElementById('task-title');
        if (taskTitle) {
            taskTitle.textContent = task.title || 'Без названия';
        }

        // Статус и приоритет
        const taskStatus = document.getElementById('task-status');
        if (taskStatus) {
            taskStatus.textContent = task.status?.status || 'Не указано';

            // Очищаем все классы статуса
            taskStatus.className = 'status-badge';

            // Добавляем соответствующий класс для статуса
            if (task.status?.id === 5) {
                taskStatus.classList.add('status-overdue');
                console.log('Статус задачи: просроченный (id=5)');
            }
        }

        const taskPriority = document.getElementById('task-priority');
        if (taskPriority) {
            taskPriority.textContent = task.priority ? `Приоритет: ${task.priority}` : 'Приоритет не указан';
        }

        // Описание задачи
        const taskDescription = document.getElementById('task-description');
        if (taskDescription) {
            taskDescription.textContent = task.description || 'Описание отсутствует';
        }

        // Даты
        document.getElementById('task-startDate').textContent = formatDate(task.startDate);
        document.getElementById('task-deadline').textContent = formatDate(task.deadline);
        document.getElementById('task-updateDate').textContent = formatDate(task.updateDate);

        // Категория
        document.getElementById('task-category').textContent = task.category?.name || 'Не указана';

        // Люди

        document.getElementById('task-creator').textContent = task.creator ?
            `${task.creator.name} ${task.creator.surname}` : 'Не указан';

        // Проект
        const projectName = await fetchProject(task.projectId);
        document.getElementById('task-project').textContent = projectName;

        return task;

    } catch (error) {
        console.error('Ошибка:', error);
        alert('Не удалось загрузить данные задачи. Пожалуйста, попробуйте позже.');
        throw error;
    }
}

// Функция для добавления новой заметки
async function addNote() {
    const noteTitle = document.getElementById('note-title')?.value.trim() || '';
    const noteContent = document.getElementById('new-note')?.value.trim() || '';
    const charCounter = document.getElementById('char-count');

    if (charCounter) {
        charCounter.textContent = noteContent.length;
    }

    if (noteTitle.length > 50 || noteTitle.length < 3) {
        alert('Заголовок заметки не должен превышать 50 символов и быть не короче 3 символов');
        return;
    }

    if (noteContent.length > 1000) {
        alert('Текст заметки не должен превышать 1000 символов');
        return;
    }

    try {
        const response = await fetchWithAuth('/task-manager-api/task-notes', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                title: noteTitle || 'Новая заметка',
                content: noteContent,
                creatorId: 1,
                taskId: parseInt(taskId)
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Ошибка при сохранении заметки');
        }

        const notes = await fetchTaskNotes(taskId);
        displayNotes(notes);

        document.getElementById('note-title').value = '';
        document.getElementById('new-note').value = '';
        if (charCounter) {
            charCounter.textContent = '0';
        }

    } catch (error) {
        console.error('Ошибка:', error);
        alert(error.message || 'Не удалось сохранить заметку');
    }
}

// Инициализация счетчика символов
function initCharCounter() {
    const textarea = document.getElementById('new-note');
    const counter = document.getElementById('char-count');

    if (textarea && counter) {
        textarea.addEventListener('input', function() {
            counter.textContent = this.value.length;
        });
    }
}

// Основная функция инициализации страницы
async function initializePage() {
    try {
        initCharCounter();
        await fetchTaskDetails(taskId);
        const notes = await fetchTaskNotes(taskId);
        displayNotes(notes);

        const addButton = document.getElementById('add-note-btn');
        if (addButton) {
            addButton.addEventListener('click', addNote);
        }

    } catch (error) {
        console.error('Ошибка инициализации:', error);
    }
}

document.addEventListener('DOMContentLoaded', initializePage);