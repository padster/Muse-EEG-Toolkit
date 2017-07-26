package eeg.useit.today.eegtoolkit.view;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

/**
 * RecyclerView holder that wraps a data-bound view, allowing each recycled view to
 * be separately bound to the child item it contains.
 *
 * @param <T> Binding for the child view.
 */
public class CustomViewHolder<T extends ViewDataBinding> extends RecyclerView.ViewHolder {
  private final T binding;

  /** Holdings a binding for the view. */
  public CustomViewHolder(T binding) {
    super(binding.getRoot());
    this.binding = binding;
  }

  /** @return The binding. */
  public T getBinding() {
    return this.binding;
  }
}
