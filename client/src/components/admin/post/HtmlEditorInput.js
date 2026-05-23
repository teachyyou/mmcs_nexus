import React, { useEffect, useRef } from 'react';
import { useInput } from 'react-admin';
import { Box, Typography } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import Quill from 'quill';
import 'quill/dist/quill.snow.css';

const toolbarOptions = [
    [{ header: [1, 2, 3, false] }],
    ['bold', 'italic', 'underline', 'strike'],
    [{ list: 'ordered' }, { list: 'bullet' }],
    ['blockquote', 'code-block'],
    ['link'],
    ['clean'],
];

const HtmlEditorInput = ({ source, label, validate }) => {
    const theme = useTheme();
    const editorRef = useRef(null);
    const quillRef = useRef(null);

    const {
        field,
        fieldState: { error },
    } = useInput({ source, validate });

    useEffect(() => {
        if (!editorRef.current || quillRef.current) {
            return;
        }

        const quill = new Quill(editorRef.current, {
            theme: 'snow',
            modules: {
                toolbar: toolbarOptions,
            },
        });

        quill.root.innerHTML = field.value || '';

        quill.on('text-change', () => {
            field.onChange(quill.root.innerHTML);
        });

        quillRef.current = quill;
    }, []);

    useEffect(() => {
        const quill = quillRef.current;

        if (!quill) {
            return;
        }

        const currentHtml = quill.root.innerHTML;
        const nextHtml = field.value || '';

        if (nextHtml && currentHtml !== nextHtml) {
            quill.root.innerHTML = nextHtml;
        }
    }, [field.value]);

    return (
        <Box
            sx={{
                width: '100%',

                '& .ql-toolbar': {
                    borderColor: theme.palette.divider,
                    borderTopLeftRadius: 8,
                    borderTopRightRadius: 8,
                    backgroundColor: theme.palette.background.paper,
                },

                '& .ql-container': {
                    minHeight: 260,
                    borderColor: theme.palette.divider,
                    borderBottomLeftRadius: 8,
                    borderBottomRightRadius: 8,
                    backgroundColor: theme.palette.background.paper,
                    color: theme.palette.text.primary,
                },

                '& .ql-editor': {
                    minHeight: 260,
                    color: theme.palette.text.primary,
                },

                '& .ql-editor.ql-blank::before': {
                    color: theme.palette.text.secondary,
                },

                '& .ql-stroke': {
                    stroke: theme.palette.text.primary,
                },

                '& .ql-fill': {
                    fill: theme.palette.text.primary,
                },

                '& .ql-picker': {
                    color: theme.palette.text.primary,
                },

                '& .ql-picker-options': {
                    backgroundColor: theme.palette.background.paper,
                    borderColor: theme.palette.divider,
                },

                '& pre': {
                    p: 2,
                    borderRadius: 1,
                    overflowX: 'auto',
                    backgroundColor: theme.palette.mode === 'dark' ? '#111827' : '#f3f4f6',
                    color: theme.palette.text.primary,
                },
            }}
        >
            <Typography variant="subtitle2" sx={{ mb: 1 }}>
                {label}
            </Typography>

            <Box ref={editorRef} />

            {error && (
                <Typography variant="caption" color="error" sx={{ mt: 0.5, display: 'block' }}>
                    {error.message}
                </Typography>
            )}
        </Box>
    );
};

export default HtmlEditorInput;