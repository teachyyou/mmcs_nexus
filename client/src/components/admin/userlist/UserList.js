import React from 'react';
import {Datagrid, List, TextField} from 'react-admin';

const UserList = (props) => {
    return (
        <List {...props}>
            <Datagrid>
                <TextField source="firstName" label="Имя" />
                <TextField source="lastName" label="Фамилия" />
                <TextField source="login" label="Логин" />
                <TextField source="userGroup" label="Группа" />
                <TextField source="status" label="Статус" />
                <TextField source="role" label="Роль" />
            </Datagrid>
        </List>
    );
}

export default UserList;
