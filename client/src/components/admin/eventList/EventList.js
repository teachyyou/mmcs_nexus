import React from 'react';
import { List, Datagrid, TextField } from 'react-admin';

const EventList = (props) => {
    return (
        <List {...props}>
            <Datagrid>
                <TextField source="id" />
                <TextField source="name" label="Name" />
                <TextField source="eventType" label="Type" />
                <TextField source="year" label="Year" />
            </Datagrid>
        </List>
    );
}

export default EventList;
