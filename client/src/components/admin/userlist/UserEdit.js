import React from 'react';
import { Edit, SimpleForm, TextInput, SelectInput, required } from 'react-admin';

const UserEdit = (props) => {
    return (
        <Edit title="Edit User" {...props}>
            <SimpleForm>
                <TextInput
                    source="login"
                    name="login"
                    inputProps={{ readOnly: true }}
                    style={{ backgroundColor: '#f0f0f0' }}
                />
                <TextInput
                    source="firstName"
                    name="firstName"
                />
                <TextInput
                    source="lastName"
                    name="lastName"
                />
                <TextInput
                    source="userGroup"
                    name="userGroup"
                />
                <SelectInput
                    source="status"
                    name="status"
                    choices={[
                        { id: 'VERIFIED', name: 'Verified' },
                        { id: 'NON_VERIFIED', name: 'Non-Verified' },
                        { id: 'BLOCKED', name: 'Blocked' }
                    ]}
                    validate={required()}
                />
                <SelectInput
                    source="role"
                    name="role"
                    choices={[
                        { id: 'ROLE_USER', name: 'User' },
                        { id: 'ROLE_ADMIN', name: 'Administrator' }
                    ]}
                    validate={required()}
                />
            </SimpleForm>
        </Edit>
    );
};

export default UserEdit;
