import React from 'react';
import { Edit, SimpleForm, TextInput, SelectInput, required } from 'react-admin';

const EventEdit = (props) => {
    return (
        <Edit title="Edit Project" {...props}>
            <SimpleForm>
                <TextInput
                    source="id"
                    name="id"
                    inputProps={{ readOnly: true }}
                    style={{ backgroundColor: '#f0f0f0' }}
                />
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
                    source="type"
                    name="type"
                    choices={[
                        { id: 'IDEA', name: 'Idea' },
                        { id: 'ZERO_VERSION', name: 'Zero Version' },
                        { id: 'PRE_RELEASE', name: 'Pre-Release' },
                        { id: 'RELEASE', name: 'Release' },
                    ]}
                    validate={required()}
                />

            </SimpleForm>
        </Edit>
    );
};

export default EventEdit;
