import { render, fireEvent, waitFor } from '@testing-library/svelte';
import { describe, it, expect, vi } from 'vitest';
import AuthModal from './AuthModal.svelte';

describe('AuthModal component', () => {
  it('renders modal with username, password fields and buttons when isOpen is true', async () => {
    const { getByTestId, queryByTestId } = render(AuthModal, {
      props: { isOpen: true, username: 'admin' }
    });

    expect(getByTestId('auth-modal')).toBeDefined();
    expect(getByTestId('auth-username-input')).toBeDefined();
    expect(getByTestId('auth-password-input')).toBeDefined();
    expect(getByTestId('auth-submit-btn')).toBeDefined();
    expect(getByTestId('auth-cancel-btn')).toBeDefined();
    expect(getByTestId('auth-username-input').value).toBe('admin');
  });

  it('does not render modal when isOpen is false', () => {
    const { queryByTestId } = render(AuthModal, {
      props: { isOpen: false }
    });

    expect(queryByTestId('auth-modal')).toBeNull();
  });

  it('shows error when submitting without username or password', async () => {
    const { getByTestId, queryByTestId } = render(AuthModal, {
      props: { isOpen: true, username: '', password: '' }
    });

    const submitBtn = getByTestId('auth-submit-btn');
    await fireEvent.click(submitBtn);

    expect(getByTestId('auth-error-message')).toBeDefined();
    expect(getByTestId('auth-error-message').textContent).toContain('Username is required');
  });

  it('displays visible success feedback on successful login', async () => {
    const { getByTestId } = render(AuthModal, {
      props: { isOpen: true, username: 'admin', password: 'secretpassword' }
    });

    const submitBtn = getByTestId('auth-submit-btn');
    await fireEvent.click(submitBtn);

    expect(getByTestId('auth-success-message')).toBeDefined();
    expect(getByTestId('auth-success-message').textContent).toContain('Authentication successful');
  });

  it('displays visible error feedback and preserves typed username on failed login attempt', async () => {
    const { getByTestId } = render(AuthModal, {
      props: { isOpen: true, username: 'typed_user_john', password: 'wrong' }
    });

    const usernameInput = getByTestId('auth-username-input');
    const submitBtn = getByTestId('auth-submit-btn');

    await fireEvent.click(submitBtn);

    // Visible error feedback
    expect(getByTestId('auth-error-message')).toBeDefined();
    expect(getByTestId('auth-error-message').textContent).toContain('Invalid credentials');

    // User's typed username survives!
    expect(usernameInput.value).toBe('typed_user_john');
  });

  it('closes modal when Escape key is pressed', async () => {
    let closed = false;
    const onClose = () => { closed = true; };

    render(AuthModal, {
      props: { isOpen: true, username: 'admin', onClose }
    });

    await fireEvent.keyDown(window, { key: 'Escape' });
    expect(closed).toBe(true);
  });
});
