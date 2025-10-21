// client/src/components/admin/i18nProvider.js
import polyglotI18nProvider from 'ra-i18n-polyglot';

const ru = {
    ra: {
        sort: {
            ASC: 'возр.',
            DESC: 'убыв.',
        },
        action: {
            add: 'Добавить',
            sort: 'Сортировать',
            edit: 'Редактировать',
            save: 'Сохранить',
            delete: 'Удалить',
            show: 'Показать',
            list: 'Список',
            cancel: 'Отмена',
            undo: 'Отменить',
            refresh: 'Обновить',
            create: 'Добавить',
            search: 'Поиск',
            export: 'Экспорт',
            select_all: 'Выбрать всё',
            select_row: 'Выбрать',
            unselect: 'Снять выделение',
            unselect_row: 'Снять выделение',
            confirm: 'Подтвердить',
            bulk_actions: 'Выбрано: %{smart_count}',
        },
        boolean: {
            true: 'Да',
            false: 'Нет',
        },
        page: {
            list: '%{name}',
            edit: '%{name} #%{id}',
            create: 'Создать %{name}',
            show: '%{name} #%{id}',
            empty: 'Нет записей %{name}',
            invite: 'Добавить новую',
        },
        navigation: {
            no_results: 'Ничего не найдено',
            page_rows_per_page: 'Строк на странице',
            // ВАЖНО: именно эти два ключа использует RA
            page_range_info:
                '%{offsetBegin}–%{offsetEnd} из %{total}',
            partial_page_range_info:
                '%{offsetBegin}–%{offsetEnd} из более чем %{offsetEnd}',
            next: 'Следующая',
            prev: 'Предыдущая',
            skip_nav: 'К контенту',
        },
        notification: {
            deleted: 'Запрос отправлен',
        },
        validation: {
            email: 'Неверный адрес почты',
        },
        message: {
            delete_title: 'Подтвердите удаление',
            delete_content: 'Это действие отменить нельзя',
            yes: 'Да',
            invalid_form: 'Произошла ошибка, проверьте введенные данные',
            no: 'Нет',
            are_you_sure: 'Вы уверены?',
            bulk_delete_title: 'Удалить %{name}',
            bulk_delete_content:
                'Вы действительно хотите удалить %{smart_count} элемент(ов)?',
        },
    },
    resources: {
        users: { name: 'Пользователи' },
        projects: { name: 'Проекты' },
        events: { name: 'События' },
    },
};

const i18nProvider = polyglotI18nProvider(() => ru, 'ru');
export default i18nProvider;
