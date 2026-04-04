// js/utils.js
const TransaccionHelper = {
    timeoutId: null,
    isCompleted: false,

    iniciarTimeout(callback, ms = 5000) {
        this.isCompleted = false;
        this.timeoutId = setTimeout(() => {
            if (!this.isCompleted) {
                callback();
            }
        }, ms);
    },

    cancelarTimeout() {
        if (this.timeoutId) {
            clearTimeout(this.timeoutId);
            this.timeoutId = null;
        }
        this.isCompleted = true;
    },

    marcarCompletado() {
        this.isCompleted = true;
        if (this.timeoutId) {
            clearTimeout(this.timeoutId);
            this.timeoutId = null;
        }
    }
};

window.TransaccionHelper = TransaccionHelper;