import React from 'react';
import {
    TextInput,
    SelectInput,
    required,
    Create,
    useNotify,
    useRedirect,
    useDataProvider, SimpleForm
} from 'react-admin';

const ProjectCreate = (props) => {
    const notify = useNotify();
    const redirect = useRedirect();
    const dataProvider = useDataProvider();

    const handleSubmit = async (data) => {
        dataProvider.create('projects', { data })
            .then(() => {
                notify('Сохранено успешно');
                redirect('list', 'projects');
            })
            .catch((error) => {
                if (error.status === 409) {
                    notify('Проект с таким названием уже существует', { type: 'warning' });
                } else {
                    notify('Неизвестная ошибка', { type: 'error' });
                }
            });
    };

    return (
        <Create title="Добавить проект" {...props} redirect="list">
            <SimpleForm onSubmit={handleSubmit}>
                <TextInput
                    source="name"
                    name="name"
                    validate={required()}
                />
                <TextInput
                    multiline
                    source="description"
                    name="description"
                    validate={required()}
                />
                <TextInput
                    source="year"
                    name="year"
                    validate={required()}
                />
                <SelectInput
                    source="type"
                    name="type"
                    choices={[
                        { id: 'WEB_APP', name: 'Web Application' },
                        { id: 'DESKTOP_APP', name: 'Desktop Application' },
                        { id: 'MOBILE_APP', name: 'Mobile Application' },
                        { id: 'GAME', name: 'Game' },
                        { id: 'GAME_MOD', name: 'Mod' },
                        { id: 'TELEGRAM_BOT', name: 'Telegram Bot' }
                    ]}
                    validate={required()}

                />
            </SimpleForm>
        </Create>
    );
};

export default ProjectCreate;
