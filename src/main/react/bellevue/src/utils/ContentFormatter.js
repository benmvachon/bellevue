import MarkdownIt from 'markdown-it';
import DOMPurify from 'dompurify';

const md = new MarkdownIt({
  html: false,
  linkify: true
});

export const formatContent = (content) => {
  return DOMPurify.sanitize(md.render(content));
};
