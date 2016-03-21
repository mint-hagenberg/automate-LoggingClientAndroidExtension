/*
 *     Copyright (C) 2016 Mobile Interactive Systems Research Group
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.R;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.KernelBase;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.Manager;

/**
 * Adapter to list all managers.
 */
public class ManagerAdapter extends RecyclerView.Adapter<ManagerAdapter.ViewHolder> {
	public static class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
		public TextView mTextView;
		public SwitchCompat mCheckbox;

		public Manager mManager;

		public ViewHolder(View v) {
			super(v);
			mTextView = (TextView) v.findViewById(R.id.text);
			mCheckbox = (SwitchCompat) v.findViewById(R.id.checkbox);
			mCheckbox.setOnCheckedChangeListener(this);
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (mCheckbox.getTag() != null) {
				mCheckbox.setTag(null);
				return;
			}

			if (mManager != null) {
				if (mManager.getStatus() == Manager.Status.STARTED) {
					mManager.pause();
				} else {
					mManager.resume();
				}
			}
		}
	}

	public ManagerAdapter() {
		// Nothing to do here
	}

	@Override
	public int getItemCount() {
		return KernelBase.isInitialized() ? KernelBase.getKernel().numManager() : 0;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.listitem_manager, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.mManager = KernelBase.getKernel().getManager(position);
		holder.mTextView.setText(holder.mManager.getName());
		holder.mCheckbox.setTag(true);
		holder.mCheckbox.setChecked(holder.mManager.getStatus() == Manager.Status.STARTED);
		//holder.mCheckbox.setVisibility(holder.mManager.getClass().isAnnotationPresent(ExternalManager.class) ? View.GONE : View.VISIBLE);
	}
}
