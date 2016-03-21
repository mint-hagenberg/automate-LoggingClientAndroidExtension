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

package at.fh.hagenberg.mint.automate.loggingclient.androidextension.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import at.fh.hagenberg.mint.automate.loggingclient.androidextension.R;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.ui.adapter.ManagerAdapter;
import at.fh.hagenberg.mint.automate.loggingclient.androidextension.util.KernelManagerHelper;
import at.fhhagenberg.mint.automate.loggingclient.javacore.kernel.KernelBase;

/**
 * A simple activity that lists all registered managers with the possibility to disable or enable them.
 */
public class ManagerListActivity extends AppCompatActivity {
private RecyclerView mRecyclerView;
	private RecyclerView.Adapter mAdapter;
	private RecyclerView.LayoutManager mLayoutManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manager_list);
		mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

		try {
			KernelBase.initialize(KernelManagerHelper.initializeKernel(this));
			KernelBase.getKernel().startup();
			Log.d(ManagerListActivity.class.getSimpleName(), "Kernel should be init " + KernelBase.isInitialized() + " and " + KernelBase.isKernelUpRunning());
		} catch (Exception e) {
			e.printStackTrace();
		}

		mRecyclerView.setHasFixedSize(true);

		mLayoutManager = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(mLayoutManager);

		mAdapter = new ManagerAdapter();
		mRecyclerView.setAdapter(mAdapter);
	}
}
