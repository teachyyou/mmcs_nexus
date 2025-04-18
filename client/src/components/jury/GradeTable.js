import React, { useState, useEffect } from 'react';
import {
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Box,
    Link as MuiLink,
    Card
} from '@mui/material';
import { Link } from 'react-router-dom';
import GradeEditorModal from './GradeEditorModal';
import InlineGradeEditor from './InlineGradeEditor';
import { useAuth } from "../../AuthContext";

const cellSx = {
    border: '2px solid black',
    p: 0.5,
    maxWidth: 120,
    minHeight: 60,
    overflow: 'hidden'
};

const GradeTable = ({ grades, event }) => {
    const [localGrades, setLocalGrades] = useState(grades);
    const [selectedGrade, setSelectedGrade] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedJuryId, setSelectedJuryId] = useState(null);
    const [selectedProjectId, setSelectedProjectId] = useState(null);
    const { userId } = useAuth();

    useEffect(() => {
        setLocalGrades(grades);
    }, [grades]);

    const handleCellClick = (grade, projectId, juryId) => {
        setSelectedGrade(grade);
        setSelectedJuryId(juryId);
        setSelectedProjectId(projectId);
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setSelectedGrade(null);
        setSelectedJuryId(null);
        setSelectedProjectId(null);
    };

    const handleGradeUpdated = (updatedGrade) => {
        const projId = updatedGrade.projectId;
        const jurId = updatedGrade.juryId;
        const newRows = localGrades.rows.map((row) => {
            if (row.projectId === projId) {
                const newTableRow = row.tableRow.slice();
                const idx = newTableRow.findIndex(item => item.juryId === jurId);
                if (idx >= 0) {
                    newTableRow[idx] = updatedGrade;
                } else {
                    newTableRow.push(updatedGrade);
                }
                return { ...row, tableRow: newTableRow };
            }
            return row;
        });
        setLocalGrades({ ...localGrades, rows: newRows });
    };

    const handleInlineUpdate = async (gradeItem, field, newValue) => {
        const oldValue = gradeItem[field];
        const updatedGrade = { ...gradeItem, [field]: newValue };

        try {
            const response = await fetch('http://localhost:8080/api/v1/jury/grades', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(updatedGrade)
            });

            if (!response.ok) {
                const errorData = await response.json();
                console.error('Ошибка при сохранении оценки:', errorData);
            }
            const updatedGradeFromServer = await response.json();
            handleGradeUpdated(updatedGradeFromServer);
        } catch (error) {
            return { success: false, oldValue };
        }
        return { success: true };
    };

    // Словарь для быстрого доступа к оценкам по projectId и juryId
    const gradeMap = {};
    if (localGrades && localGrades.rows) {
        localGrades.rows.forEach((row) => {
            gradeMap[row.projectId] = {};
            row.tableRow.forEach((item) => {
                gradeMap[row.projectId][item.juryId] = { ...item };
            });
        });
    }

    return (
        <>
            <TableContainer component={Card} sx={{ mt: 2 }}>
                <Table sx={{ minWidth: 600, tableLayout: 'flexible', width: '100%' }}>
                    <TableHead>
                        <TableRow>
                            <TableCell sx={{ ...cellSx, fontWeight: 'bold' }}>
                                Проекты / Жюри
                            </TableCell>
                            {localGrades.juries.map(jury => (
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
                        {localGrades.rows.map(rowData => {
                            const { projectId, mentorId } = rowData;
                            const project = localGrades.projects.find(p => p.id === projectId);
                            return (
                                <TableRow key={projectId}>
                                    <TableCell sx={{ ...cellSx, fontWeight: 'bold' }}>
                                        {project?.name}
                                    </TableCell>
                                    {localGrades.juries.map(jury => {
                                        const gradeItem = gradeMap[projectId]?.[jury.id];
                                        const isOwner = jury.id === userId;
                                        const isMentorCell = mentorId === userId && jury.id === userId;

                                        if (isMentorCell) {
                                            return (
                                                <TableCell
                                                    key={`${projectId}-${jury.id}`} align="center" sx={cellSx}
                                                >
                                                    <Box sx={{ color: '#999' }}>Недоступно</Box>
                                                </TableCell>
                                            );
                                        }

                                        // Обычная логика для остальных пользователей
                                        return (
                                            <TableCell
                                                key={`${projectId}-${jury.id}`} align="center" sx={cellSx}
                                            >
                                                {gradeItem ? (
                                                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                                                        <InlineGradeEditor
                                                            gradeItem={gradeItem}
                                                            maxBuild={event.maxBuildPoints}
                                                            maxPres={event.maxPresPoints}
                                                            onUpdate={(field, newValue) =>
                                                                handleInlineUpdate(gradeItem, field, newValue)
                                                            }
                                                        />
                                                        <MuiLink
                                                            component={Link}
                                                            to="#"
                                                            onClick={() => handleCellClick(gradeItem, projectId, jury.id)}
                                                            sx={{ fontSize: '0.75rem', color: 'blue' }}
                                                        >
                                                            Подробнее...
                                                        </MuiLink>
                                                    </Box>
                                                ) : (
                                                    isOwner ? (
                                                        <MuiLink
                                                            component={Link}
                                                            to="#"
                                                            onClick={() => handleCellClick(null, projectId, jury.id)}
                                                            sx={{ fontSize: '0.75rem', color: 'blue' }}
                                                        >
                                                            Не оценено
                                                        </MuiLink>
                                                    ) : (
                                                        <Box sx={{ fontSize: '0.75rem', color: '#999' }}>
                                                            Не оценено
                                                        </Box>
                                                    )
                                                )}
                                            </TableCell>
                                        );
                                    })}
                                </TableRow>
                            );
                        })}
                    </TableBody>
                </Table>
            </TableContainer>
            <GradeEditorModal
                open={isModalOpen}
                onClose={handleCloseModal}
                grade={selectedGrade}
                projectId={selectedProjectId}
                juryId={selectedJuryId}
                grades={localGrades}
                onGradeUpdated={handleGradeUpdated}
            />
        </>
    );
};

export default GradeTable;
