import React from 'react';
import {SimpleForm, TextInput, SelectInput, required, Create} from 'react-admin';

const EventCreate = (props) => {
    return (
        <Create title="Create Project" {...props} redirect="list">
            <SimpleForm>
                <TextInput
                    source="name"
                    name="name"
                    validate={required()}
                />
                <SelectInput
                    source="eventType"
                    name="eventType"
                    choices={[
                        { id: 'IDEA', name: 'Idea' },
                        { id: 'ZERO_VERSION', name: 'Zero Version' },
                        { id: 'PRE_RELEASE', name: 'Pre-Release' },
                        { id: 'RELEASE', name: 'Release' },
                    ]}
                    validate={required()}
                />
                <TextInput
                    source="year"
                    name="year"
                    validate={required()}
                />
            </SimpleForm>
        </Create>
    );
};

export default EventCreate;
