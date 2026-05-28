import { motion, AnimatePresence } from 'framer-motion';
import { AlertTriangle, X } from 'lucide-react';
import './ConfirmationModal.css';

const ConfirmationModal = ({ 
  isOpen, 
  onClose, 
  onConfirm, 
  title = 'Confirmar Ação', 
  message = 'Tem certeza que deseja realizar esta ação?', 
  confirmText = 'Confirmar', 
  cancelText = 'Cancelar',
  variant = 'danger' // 'danger' | 'warning' | 'info'
}) => {
  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <div className="modal-overlay" onClick={onClose}>
        <motion.div 
          className="modal-card confirmation-card glass"
          initial={{ opacity: 0, scale: 0.9, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.9, y: 20 }}
          transition={{ type: 'spring', damping: 25, stiffness: 300 }}
          onClick={e => e.stopPropagation()}
        >
          <div className="confirmation-header">
            <div className={`icon-wrapper ${variant}`}>
              <AlertTriangle size={24} />
            </div>
            <button className="close-btn" onClick={onClose}><X size={20} /></button>
          </div>

          <div className="confirmation-body">
            <h3>{title}</h3>
            <p>{message}</p>
          </div>

          <div className="confirmation-actions">
            <button className="btn-secondary" onClick={onClose}>
              {cancelText}
            </button>
            <button className={`btn-confirm ${variant}`} onClick={onConfirm}>
              {confirmText}
            </button>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
};

export default ConfirmationModal;
