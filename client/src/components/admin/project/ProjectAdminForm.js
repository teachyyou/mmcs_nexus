import {
    required,
    SelectInput,
    SimpleForm,
    TextInput,
    useNotify,
    useRecordContext,
    useRedirect
} from "react-admin";
import AdminToolbar from "../AdminToolbar";
import React from "react";

const ProjectAdminForm = (props) => {

    const { requestMethod } = props;
    const notify = useNotify();
    const redirect = useRedirect();
    const record = useRecordContext();

    const handleSubmit = async (data) => {

        const { id, ...dataWithoutId } = data;
        const params = record?.id ? { id: record.id, data: dataWithoutId } : { data: dataWithoutId };

        requestMethod('projects', params)
            .then(() => {
                notify('Сохранено успешно');
                redirect('list', 'projects');
            })
            .catch((error) => {
                if (error.status === 409) {
                    notify('Проект с такими названием уже существует', { type: 'warning' });
                } else {
                    notify('Неизвестная ошибка', { type: 'error' });
                }
            });
    };

    return (
        <SimpleForm onSubmit={handleSubmit} toolbar={<AdminToolbar />}>
            <TextInput
                source="name"
                name="name"
                label="Название"
                validate={required()}
            />
            <TextInput
                multiline
                source="description"
                name="description"
                label="Описание"
                validate={required()}
            />
            <TextInput
                source="year"
                name="year"
                label="Год"
                validate={required()}
            />
            <SelectInput
                source="type"
                name="type"
                label="Тип проекта"
                choices={[
                    { id: 'WEB_APP', name: 'Веб-приложение' },
                    { id: 'DESKTOP_APP', name: 'Десктопное приложение' },
                    { id: 'MOBILE_APP', name: 'Мобильное приложение' },
                    { id: 'GAME', name: 'Игра' },
                    { id: 'MOD', name: 'Мод' },
                    { id: 'TELEGRAM_BOT', name: 'Телеграм-бот' }
                ]}
                validate={required()}

            />
        </SimpleForm>
    );
};

export default ProjectAdminForm;