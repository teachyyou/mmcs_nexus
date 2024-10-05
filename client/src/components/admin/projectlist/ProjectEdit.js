import React from 'react';
import { Edit, SimpleForm, TextInput, SelectInput, required } from 'react-admin';

const ProjectEdit = (props) => {
    return (
        <Edit title="Edit Project" {...props}>
            <SimpleForm>
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
        </Edit>
    );
};

export default ProjectEdit;
