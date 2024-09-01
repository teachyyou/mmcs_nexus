import React from 'react';
import { List, Datagrid, TextField } from 'react-admin';

const UserList = (props) => {
    console.log("wowowo")
    return (
        <List {...props}>
            <Datagrid>
                <TextField source="id" />
                <TextField source="first_name" label="First Name" />
                <TextField source="last_name" label="Last Name" />
                <TextField source="login" label="Login" />
                <TextField source="user_group" label="User Group" />
                <TextField source="status" label="Status" />
                <TextField source="role" label="Role" />
            </Datagrid>
        </List>
    );
}

export default UserList;
