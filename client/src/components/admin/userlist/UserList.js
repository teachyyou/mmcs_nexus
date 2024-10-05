import React from 'react';
import { List, Datagrid, TextField } from 'react-admin';

const UserList = (props) => {
    return (
        <List {...props}>
            <Datagrid>
                <TextField source="firstName" label="First Name" />
                <TextField source="lastName" label="Last Name" />
                <TextField source="login" label="Login" />
                <TextField source="userGroup" label="User Group" />
                <TextField source="status" label="Status" />
                <TextField source="role" label="Role" />
            </Datagrid>
        </List>
    );
}

export default UserList;
