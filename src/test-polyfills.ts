/**
 * Polyfill for 'global' needed by some libraries like 'buffer' or 'sockjs-client'
 * in the browser environment (Karma/Chrome).
 */
(window as any).global = window;
