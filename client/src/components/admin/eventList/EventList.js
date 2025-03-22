import React from 'react';
import {List, Datagrid, TextField, Empty} from 'react-admin';

const EventList = (props) => {
    return (
        <List {...props}>
            <Datagrid>
                <TextField source="name" label="Name" />
                <TextField source="eventType" label="Type" />
                <TextField source="year" label="Year" />
            </Datagrid>
            <Empty>

            </Empty>
        </List>
    );
}

export default EventList;
