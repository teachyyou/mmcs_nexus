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
                label="Название"
                validate={required()}
                sx={{ maxWidth: 720 }}
            />

            <TextInput
                source="description"
                label="Описание"
                multiline
                minRows={4}
                maxRows={10}
                sx={{ maxWidth: 720 }}
                validate={required()}
            />

            <TextInput
                source="year"
                label="Год"
                validate={required()}
                sx={{ maxWidth: 240 }}
            />

            <SelectInput
                source="type"
                label="Тип проекта"
                choices={[
                    { id: 'WEB_APP', name: 'Веб-приложение' },
                    { id: 'DESKTOP_APP', name: 'Десктопное приложение' },
                    { id: 'MOBILE_APP', name: 'Мобильное приложение' },
                    { id: 'GAME', name: 'Игра' },
                    { id: 'GAME_MOD', name: 'Мод' },
                    { id: 'TELEGRAM_BOT', name: 'Телеграм-бот' },
                ]}
                validate={required()}
                sx={{ maxWidth: 360 }}
            />
        </SimpleForm>
    );
};

export default ProjectAdminForm;