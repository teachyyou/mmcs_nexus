import React from 'react';
import { Edit, SimpleForm, TextInput, SelectInput, required } from 'react-admin';

const EventEdit = (props) => {
    return (
        <Edit title="Изменить событие" {...props}>
            <SimpleForm>
                <TextInput
                    source="name"
                    name="name"
                    validate={required()}
                />
                <TextInput
                    source="year"
                    name="year"
                    validate={required()}
                />
                <SelectInput
                    source="eventType"
                    name="eventType"
                    choices={[
                        { id: 'IDEA', name: 'Защита идеи' },
                        { id: 'ZERO_VERSION', name: 'Защита нулевой версии' },
                        { id: 'PRE_RELEASE', name: 'Предзащита' },
                        { id: 'RELEASE', name: 'Итоговая защита' },
                    ]}
                    validate={required()}
                />

            </SimpleForm>
        </Edit>
    );
};

export default EventEdit;
