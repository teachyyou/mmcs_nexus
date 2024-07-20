import React from 'react';
import NavigationBar from './NavigationBar';
import {StyledEngineProvider} from "@mui/material";

const HomePage = ({ isAuthenticated, setIsAuthenticated }) => {
    return (
        <div className="App">
            <header className="App-header">
                <StyledEngineProvider injectFirst>
                    <NavigationBar className='navigation-bar' isAuthenticated={isAuthenticated} setIsAuthenticated={setIsAuthenticated} />
                </StyledEngineProvider>
            </header>

        </div>
    );
};

export default HomePage;
