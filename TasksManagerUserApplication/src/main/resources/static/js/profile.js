import { fetchWithAuth } from './auth_utils.js';

document.addEventListener('DOMContentLoaded', async function() {
    // Получаем ID пользователя из URL (например: /profile?id=123)

    try {
        // Загружаем данные пользователя
        const response = await fetchWithAuth(`/task-manager-api/users/current-user`);
        if(!response.ok){
            showError('Не удалось загрузить данные о пользователе')
        }
        const user = await response.json();

        // Заполняем данные на странице
        document.getElementById('user-name').textContent = `${user.name} ${user.surname}`;
        document.getElementById('user-username').textContent = `@${user.username}`;
        document.getElementById('user-firstName').textContent = user.name;
        document.getElementById('user-surname').textContent = user.surname;
        document.getElementById('user-email').textContent = user.email;
        document.getElementById('user-id').textContent = user.id;

        // Устанавливаем аватар (первые буквы имени и фамилии)
        const avatar = document.getElementById('user-avatar');
        avatar.textContent = `${user.name.charAt(0)}${user.surname.charAt(0)}`;

        // Обработчик кнопки "Назад"
        document.getElementById('back-button').addEventListener('click', function(e) {
            e.preventDefault();
            window.history.back();
        });

    } catch (error) {
        console.error('Error loading user profile:', error);
        // Здесь можно добавить обработку ошибок (например, показать сообщение пользователю)
    }

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