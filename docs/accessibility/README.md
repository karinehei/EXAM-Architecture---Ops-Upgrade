# Accessibility (A11y) Documentation

## Overview

The EXAM system is designed to be accessible to all users, including those using assistive technologies. This documentation describes our accessibility approach, testing methods, and compliance status.

## Accessibility Standards

We aim to comply with **WCAG 2.1 Level AA** standards, which include:

- **Perceivable**: Information must be presentable to users in ways they can perceive
- **Operable**: Interface components must be operable
- **Understandable**: Information and UI operation must be understandable
- **Robust**: Content must be robust enough for assistive technologies

## Current Status

### ✅ Implemented Features

1. **Semantic HTML**
   - Proper use of HTML5 semantic elements (`<header>`, `<main>`, `<article>`, `<time>`)
   - Correct heading hierarchy (h1 → h2 → h3)
   - Semantic list structure

2. **ARIA Labels and Roles**
   - `role="banner"` for header
   - `role="main"` for main content
   - `role="list"` and `role="listitem"` for lists
   - `role="status"` and `role="alert"` for dynamic content
   - `aria-live` regions for content updates
   - `aria-label` for interactive elements

3. **Keyboard Navigation**
   - All interactive elements are keyboard accessible
   - Visible focus indicators
   - Logical tab order

4. **Color Contrast**
   - Text meets WCAG AA contrast requirements (4.5:1)
   - Button colors meet contrast standards

5. **Error Handling**
   - Error messages with `role="alert"`
   - Retry functionality
   - Clear error communication

6. **Loading States**
   - Loading indicators with `aria-busy`
   - Status announcements for screen readers

## Testing

### Automated Testing

#### Using axe-core

Install dependencies:
```bash
cd frontend
npm install
```

Run accessibility scan (requires app to be running):
```bash
# Start the application first
npm start

# In another terminal, run a11y scan
npm run a11y
```

#### Using Browser DevTools

1. **Chrome DevTools**:
   - Open DevTools (F12)
   - Go to "Lighthouse" tab
   - Run "Accessibility" audit

2. **Firefox DevTools**:
   - Open DevTools (F12)
   - Go to "Accessibility" panel
   - Review accessibility tree

#### Using WAVE Extension

1. Install [WAVE browser extension](https://wave.webaim.org/extension/)
2. Navigate to the application
3. Click WAVE icon to see accessibility report

### Manual Testing

#### Keyboard Navigation

1. **Tab Navigation**:
   - Press `Tab` to move forward through interactive elements
   - Press `Shift+Tab` to move backward
   - Verify focus indicators are visible
   - Test all buttons and links

2. **Keyboard Activation**:
   - Press `Enter` or `Space` on buttons
   - Verify actions are triggered correctly

3. **Skip Links** (if implemented):
   - Press `Tab` to see skip link
   - Press `Enter` to activate

#### Screen Reader Testing

1. **NVDA (Windows)**:
   - Download from [nvaccess.org](https://www.nvaccess.org/)
   - Navigate through the application
   - Verify all content is announced
   - Check that dynamic updates are announced

2. **VoiceOver (macOS)**:
   - Enable: System Preferences → Accessibility → VoiceOver
   - Use `Cmd+F5` to toggle
   - Navigate with `VO+Arrow Keys`
   - Verify announcements

3. **JAWS (Windows)**:
   - Commercial screen reader
   - Test with trial version if available

#### Color Contrast Testing

1. **Browser Extensions**:
   - [WAVE](https://wave.webaim.org/extension/)
   - [axe DevTools](https://www.deque.com/axe/devtools/)
   - [Color Contrast Analyzer](https://www.tpgi.com/color-contrast-checker/)

2. **Online Tools**:
   - [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
   - [Colour Contrast Analyser](https://www.tpgi.com/color-contrast-checker/)

## Accessibility Checklist

### Perceivable

- [x] Text alternatives for images (when images are added)
- [x] Captions for multimedia (when multimedia is added)
- [x] Content can be presented in different ways
- [x] Text is readable and understandable
- [x] Color is not the only means of conveying information

### Operable

- [x] All functionality is keyboard accessible
- [x] No content causes seizures
- [x] Users have enough time to read and use content
- [x] Navigation is consistent and predictable
- [x] Input assistance is provided

### Understandable

- [x] Text is readable and understandable
- [x] Content appears and operates in predictable ways
- [x] Input assistance helps users avoid and correct mistakes

### Robust

- [x] Content is compatible with assistive technologies
- [x] Proper use of semantic HTML
- [x] ARIA labels where needed

## Common Issues and Fixes

### Issue: Missing ARIA Labels

**Fix**: Add `aria-label` or `aria-labelledby` attributes:
```html
<button aria-label="Retry loading reports">Retry</button>
```

### Issue: Missing Semantic HTML

**Fix**: Use semantic HTML elements:
```html
<!-- Bad -->
<div class="header">...</div>

<!-- Good -->
<header role="banner">...</header>
```

### Issue: Poor Color Contrast

**Fix**: Ensure contrast ratio meets WCAG AA (4.5:1 for normal text):
```css
/* Good contrast */
color: #2c3e50; /* Dark text on white */
background: #ffffff;
```

### Issue: Missing Focus Indicators

**Fix**: Add visible focus styles:
```css
button:focus {
  outline: 2px solid #007bff;
  outline-offset: 2px;
}
```

### Issue: Dynamic Content Not Announced

**Fix**: Use ARIA live regions:
```html
<div role="status" aria-live="polite">
  Content updated
</div>
```

## Development Guidelines

### When Adding New Components

1. **Use Semantic HTML**: Prefer semantic elements over divs
2. **Add ARIA Labels**: When semantic HTML isn't sufficient
3. **Test Keyboard Navigation**: Ensure all interactive elements are keyboard accessible
4. **Check Color Contrast**: Verify text meets contrast requirements
5. **Test with Screen Reader**: Verify content is announced correctly

### Code Review Checklist

- [ ] Semantic HTML used
- [ ] ARIA labels added where needed
- [ ] Keyboard navigation works
- [ ] Focus indicators visible
- [ ] Color contrast meets standards
- [ ] Error messages are accessible
- [ ] Loading states are announced

## CI Integration

Accessibility testing can be integrated into CI:

```yaml
# Example GitHub Actions step
- name: Run accessibility tests
  run: |
    cd frontend
    npm install
    npm start &
    sleep 10
    npm run a11y
```

## Resources

### Documentation

- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [ARIA Authoring Practices](https://www.w3.org/WAI/ARIA/apg/)
- [WebAIM: Accessibility Resources](https://webaim.org/)

### Tools

- [axe-core](https://github.com/dequelabs/axe-core) - Automated accessibility testing
- [WAVE](https://wave.webaim.org/) - Web Accessibility Evaluation Tool
- [Lighthouse](https://developers.google.com/web/tools/lighthouse) - Chrome DevTools audit
- [NVDA](https://www.nvaccess.org/) - Free screen reader
- [Color Contrast Analyzer](https://www.tpgi.com/color-contrast-checker/)

### Testing Services

- [Accessibility Insights](https://accessibilityinsights.io/) - Microsoft's accessibility testing tools
- [Pa11y](https://pa11y.org/) - Command-line accessibility testing

## Support

For accessibility questions or issues:

1. Review this documentation
2. Check [a11y-notes.md](a11y-notes.md) for specific findings
3. Consult WCAG 2.1 guidelines
4. Test with assistive technologies
5. Contact the development team if needed

## Changelog

- **2026-01-10**: Initial accessibility implementation
  - Added semantic HTML structure
  - Added ARIA labels and roles
  - Improved keyboard navigation
  - Added loading and error states
  - Documented testing process
