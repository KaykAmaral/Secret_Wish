import { motion, AnimatePresence } from 'framer-motion';
import { AlertTriangle, X } from 'lucide-react';
import './ConfirmationModal.css';

/**
 * Componente de Modal de Confirmação.
 * 
 * Utilizado para validar ações críticas (ex: excluir conta, sair de grupo, realizar sorteio).
 * Oferece variantes visuais para indicar o nível de risco da operação.
 * 
 * @param {boolean} props.isOpen Controla a visibilidade do modal.
 * @param {Function} props.onClose Callback disparado ao cancelar ou fechar o modal.
 * @param {Function} props.onConfirm Callback disparado ao confirmar a ação.
 * @param {string} props.title Título exibido no topo do modal.
 * @param {string} props.message Texto explicativo da ação.
 * @param {string} props.confirmText Texto do botão de confirmação.
 * @param {string} props.cancelText Texto do botão de cancelamento.
 * @param {string} props.variant Estilo visual: 'danger' (vermelho), 'warning' (amarelo), 'info' (azul).
 */
const ConfirmationModal = ({ 
  isOpen, 
  onClose, 
  onConfirm, 
  title = 'Confirmar Ação', 
  message = 'Tem certeza que deseja realizar esta ação?', 
  confirmText = 'Confirmar', 
  cancelText = 'Cancelar',
  variant = 'danger'
}) => {
  // Evita renderização de markup desnecessário quando o modal está fechado.
  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <div className="modal-overlay" onClick={onClose}>
        {/* motion.div provê animações suaves de entrada (scale/opacity) */}
        <motion.div 
          className="modal-card confirmation-card glass"
          initial={{ opacity: 0, scale: 0.9, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.9, y: 20 }}
          transition={{ type: 'spring', damping: 25, stiffness: 300 }}
          // Impede que cliques dentro do modal fechem o overlay (bubbling)
          onClick={e => e.stopPropagation()}
        >
          <div className="confirmation-header">
            <div className={`icon-wrapper ${variant}`}>
              <AlertTriangle size={24} />
            </div>
            <button className="close-btn" onClick={onClose} aria-label="Fechar"><X size={20} /></button>
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
