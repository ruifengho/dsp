package com.github.ruifengho.tx.service.impl;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.ruifengho.DspConstants;
import com.github.ruifengho.aop.DspTxTransactionAopInfo;
import com.github.ruifengho.modal.TxGroup;
import com.github.ruifengho.modal.TxTask;
import com.github.ruifengho.modal.TxTaskLocal;
import com.github.ruifengho.tx.service.TransactionService;
import com.github.ruifengho.tx.service.TxManagerService;
import com.github.ruifengho.util.RandomUtils;

@Service("txStartTransactionService")
public class TxStartTransactionServiceImpl implements TransactionService {

	private static final Logger logger = LoggerFactory.getLogger(TxStartTransactionServiceImpl.class);

	@Resource
	private TxManagerService txManagerService;

	@Override
	public Object execute(ProceedingJoinPoint point, String groupId, DspTxTransactionAopInfo info) throws Throwable {
		logger.debug("--->begin start transaction");
		groupId = StringUtils.isEmpty(groupId) ? RandomUtils.randomUUID() : groupId;

		logger.debug("创建了 group【{}】", groupId);

		TxTask txTask = TxGroup.createTxTask(groupId);
		TxTaskLocal.current(txTask);

		txManagerService.createTransactionGroup(groupId);
		int state = DspConstants.STATE_ROLLBACK;
		try {
			logger.debug("开始执行");
			Object result = null;
			try {
				result = point.proceed();
				state = DspConstants.STATE_COMMIT;
			} catch (Exception e) {
				state = rollbackException(e, info);
				throw e;
			}
			txManagerService.closeTransactionGroup(groupId, txTask.getTaskId(), state);
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			txManagerService.notifyTransaction(groupId, state);
			logger.debug("<---end start transaction");
		}
	}

	private int rollbackException(Throwable e, DspTxTransactionAopInfo info) {
		if (RuntimeException.class.isAssignableFrom(e.getClass())) {
			return DspConstants.STATE_ROLLBACK;
		}
		if (Error.class.isAssignableFrom(e.getClass())) {
			return DspConstants.STATE_ROLLBACK;
		}

		for (Class<? extends Throwable> clazz : info.getAnnotation().rollbackFor()) {
			if (clazz.isAssignableFrom(e.getClass())) {
				return DspConstants.STATE_ROLLBACK;
			}
		}
		for (Class<? extends Throwable> clazz : info.getAnnotation().noRollbackFor()) {
			if (clazz.isAssignableFrom(e.getClass())) {
				return DspConstants.STATE_COMMIT;
			}
		}
		return DspConstants.STATE_COMMIT;
	}

}
