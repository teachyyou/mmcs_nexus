import React from 'react';
import {SimpleForm, TextInput, SelectInput, required, Create, SaveButton, useNotify, useRedirect} from 'react-admin';
import {Toolbar} from "@mui/material";


const PostSaveButton = () => {
    const notify = useNotify();
    const redirect = useRedirect();
    const onSuccess = data => {
        notify(`Post "${data.title}" saved!`);
        redirect('/events');
    };
    return (
        <SaveButton type="button" label="Сохранить" mutationOptions={{ onSuccess }} />
    );
};

const PostEditToolbar = () => (
    <Toolbar>
        <PostSaveButton />
    </Toolbar>
);

const EventCreate = (props) => {
    return (
        <Create title="Добавить событие" {...props} redirect="list" >
            <SimpleForm toolbar={<PostEditToolbar/>}>
                <TextInput
                    source="name"
                    name="name"
                    label="Название"
                    validate={required()}
                />
                <SelectInput
                    source="eventType"
                    name="eventType"
                    label="Тип события"
                    choices={[
                        { id: 'IDEA', name: 'Защита идеи' },
                        { id: 'ZERO_VERSION', name: 'Защита нулевой версии' },
                        { id: 'PRE_RELEASE', name: 'Предзащита' },
                        { id: 'RELEASE', name: 'Итоговая защита' },
                    ]}
                    validate={required()}
                />
                <TextInput
                    source="year"
                    name="year"
                    label="Год"
                    validate={required()}
                />
            </SimpleForm>
        </Create>
    );
};

export default EventCreate;
