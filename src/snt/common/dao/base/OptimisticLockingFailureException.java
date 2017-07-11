package snt.common.dao.base;

import snt.common.business.BaseBusinessException;

public class OptimisticLockingFailureException extends BaseBusinessException
{
  private static final long serialVersionUID = 1L;
  private int updatedCount = -1;

  public OptimisticLockingFailureException(int updatedCount, String message)
  {
    super(message);
    this.updatedCount = updatedCount;
  }

  public int getUpdatedCount() {
    return this.updatedCount;
  }
}