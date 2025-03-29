import React, { useState } from 'react';
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Box,
    Link as MuiLink
} from '@mui/material';
import { Link } from 'react-router-dom';
import GradeEditorModal from './GradeEditorModal';
import InlineGradeEditor from './InlineGradeEditor';

const cellSx = {
    border: '2px solid black',
    p: 0.5,
    maxWidth: 120,
    minHeight: 60,
    overflow: 'hidden'
};

const GradeTable = ({ grades }) => {
    const [selectedGrade, setSelectedGrade] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedJuryId, setSelectedJuryId] = useState(null);
    const [selectedProjectId, setSelectedProjectId] = useState(null);

    const handleCellClick = (grade, projectId, juryId) => {
        setSelectedGrade(grade);
        setSelectedJuryId(juryId);
        setSelectedProjectId(projectId);
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
    };

    const handleInlineUpdate = (gradeItem, field, newValue) => {
        console.log(`Updating ${field} for grade ${gradeItem.id} to ${newValue}`);
        // Здесь можно вставить вызов обновления на сервер
    };

    const gradeMap = {};
    if (grades && grades.rows) {
        grades.rows.forEach(row => {
            gradeMap[row.projectId] = {};
            row.tableRow.forEach(item => {
                gradeMap[row.projectId][item.juryId] = { ...item };
            });
        });
    }

    return (
        <>
            <TableContainer component={Paper} sx={{ mt: 2 }}>
                <Table sx={{ minWidth: 600 }}>
                    <TableHead>
                        <TableRow>
                            <TableCell sx={{ ...cellSx, fontWeight: 'bold' }}>
                                Проекты / Жюри
                            </TableCell>
                            {grades.juries.map(jury => (
                                <TableCell
                                    key={jury.id}
                                    align="center"
                                    sx={{ ...cellSx, fontWeight: 'bold' }}
                                >
                                    {jury.firstName} {jury.lastName}
                                </TableCell>
                            ))}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {grades.projects.map(project => (
                            <TableRow key={project.id}>
                                <TableCell sx={{ ...cellSx, fontWeight: 'bold' }}>
                                    {project.name}
                                </TableCell>
                                {grades.juries.map(jury => {
                                    const gradeItem = gradeMap[project.id]?.[jury.id];
                                    return (
                                        <TableCell
                                            key={`${project.id}-${jury.id}`}
                                            align="center"
                                            sx={cellSx}
                                        >
                                            {gradeItem ? (
                                                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                                                    <InlineGradeEditor
                                                        gradeItem={gradeItem}
                                                        onUpdate={(field, newValue) =>
                                                            handleInlineUpdate(gradeItem, field, newValue)
                                                        }
                                                    />
                                                    <MuiLink
                                                        component={Link}
                                                        to="#"
                                                        onClick={() => handleCellClick(gradeItem, project.id, jury.id)}
                                                        sx={{ fontSize: '0.75rem', color: 'blue' }}
                                                    >
                                                        Подробнее...
                                                    </MuiLink>
                                                </Box>
                                            ) : (
                                                <MuiLink
                                                    component={Link}
                                                    to="#"
                                                    onClick={() => handleCellClick(null, project.id, jury.id)}
                                                    sx={{ fontSize: '0.75rem', color: 'blue' }}
                                                >
                                                    Не оценено
                                                </MuiLink>
                                            )}
                                        </TableCell>
                                    );
                                })}
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
            <GradeEditorModal
                open={isModalOpen}
                onClose={handleCloseModal}
                grade={selectedGrade}
                projectId={selectedProjectId}
                juryId={selectedJuryId}
                grades={grades}
            />
        </>
    );
};

export default GradeTable;
