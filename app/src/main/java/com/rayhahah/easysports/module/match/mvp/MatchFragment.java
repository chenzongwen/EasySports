package com.rayhahah.easysports.module.match.mvp;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.rayhahah.easysports.R;
import com.rayhahah.easysports.common.BaseFragment;
import com.rayhahah.easysports.common.C;
import com.rayhahah.easysports.databinding.FragmentMatchBinding;
import com.rayhahah.easysports.module.match.bean.MatchListBean;
import com.rayhahah.easysports.module.match.domain.MatchLiveListAdapter;
import com.rayhahah.easysports.view.MatchListItemDecoration;
import com.rayhahah.rbase.utils.base.DateTimeUitl;
import com.rayhahah.rbase.utils.base.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by a on 2017/5/17.
 */

public class MatchFragment extends BaseFragment<MatchPresenter, FragmentMatchBinding>
        implements MatchContract.IMatchView, BaseQuickAdapter.RequestLoadMoreListener,
        SwipeRefreshLayout.OnRefreshListener {

    private BaseQuickAdapter<MatchListBean.DataBean.MatchesBean.MatchInfoBean, BaseViewHolder> mMatchListAdapter;
    private String mFutureDate;
    private String mBeforeDate;
    private String mCurrentDate;
    private List<MatchListBean.DataBean.MatchesBean.MatchInfoBean> totalData = new ArrayList<>();
    private MatchListItemDecoration mItemDecor;

    @Override
    protected int setFragmentLayoutRes() {
        return R.layout.fragment_match;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mBinding.toolbar.tvToolbarTitle.setText(getResources().getString(R.string.match));

        initRv();

        mCurrentDate = DateTimeUitl.getCurrentWithFormate("yyyy-MM-dd");
        mCurrentDate = "2016-12-26";
        initFutureBeforeDate(mCurrentDate);
        initProgressLayout();

        showLoading(mBinding.rvMatchList, mBinding.pl);
        mPresenter.addMatchListData(mCurrentDate, C.STATUS.INIT);
    }

    @Override
    protected MatchPresenter getPresenter() {
        return new MatchPresenter(this);
    }

    /**
     * 获取比赛数据成功
     *
     * @param data   比赛数据
     * @param status 获取状态
     */
    @Override
    public void addMatchListData(final List<MatchListBean.DataBean.MatchesBean.MatchInfoBean> data, int status) {
        switch (status) {
            case C.STATUS.INIT:
                mMatchListAdapter.setNewData(data);
                if (data.size() == 0) {
                    getFutureMatchListData();
                }
                break;
            case C.STATUS.REFRESH:
                mMatchListAdapter.addData(0, data);
                mBinding.srlMatchList.setRefreshing(false);
                if (data.size() == 0) {
                    getBeforeMatchListData();
                }
                break;
            case C.STATUS.LOAD_MORE:
                mMatchListAdapter.addData(data);
                mMatchListAdapter.loadMoreComplete();
                if (data.size() == 0) {
                    getFutureMatchListData();
                }
                break;
        }
        totalData = mMatchListAdapter.getData();
        mItemDecor.setNewData(totalData);

        mMatchListAdapter.notifyDataSetChanged();
        showContent(mBinding.rvMatchList, mBinding.pl);
    }


    /**
     * 获取比赛数据失败
     *
     * @param throwable 异常原因
     * @param status    获取状态
     */
    @Override
    public void addMatchListDataFailed(Throwable throwable, int status) {
        switch (status) {
            case C.STATUS.INIT:
                break;
            case C.STATUS.REFRESH:
                mBinding.srlMatchList.setRefreshing(false);
                break;
            case C.STATUS.LOAD_MORE:
                mMatchListAdapter.loadMoreFail();
                break;
        }
        showError(mBinding.rvMatchList, mBinding.pl);
        mFutureDate = mCurrentDate;
        mBeforeDate = mCurrentDate;
    }

    @Override
    public String getCurrentDate() {
        return mCurrentDate;
    }


    /**
     * 下拉刷新加载之前的比赛数据
     */
    @Override
    public void onRefresh() {
        getBeforeMatchListData();
    }

    /**
     * 上拉加载之后的比赛数据
     */
    @Override
    public void onLoadMoreRequested() {
        getFutureMatchListData();
    }

    /**
     * 获取前一天的比赛数据
     */
    private void getBeforeMatchListData() {
        mBeforeDate = DateTimeUitl.getBeforeFromTarget(mBeforeDate);
        mPresenter.addMatchListData(mBeforeDate, C.STATUS.REFRESH);
    }

    /**
     * 获取下一天的比赛数据
     */
    private void getFutureMatchListData() {
        mFutureDate = DateTimeUitl.getFutureFromTarget(mFutureDate);
        mPresenter.addMatchListData(mFutureDate, C.STATUS.LOAD_MORE);
    }

    /**
     * 初始化列表、悬浮栏、下拉刷新
     */
    private void initRv() {
        mBinding.rvMatchList.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mMatchListAdapter = new MatchLiveListAdapter();
        mItemDecor = new MatchListItemDecoration(getActivity(), totalData
                , mThemeColorMap.get(C.ATTRS.COLOR_TEXT_DARK)
                , mThemeColorMap.get(C.ATTRS.COLOR_BG_DARK)
                , mThemeColorMap.get(C.ATTRS.COLOR_PRIMARY)
                , new MatchListItemDecoration.DecorationCallback() {
            @Override
            public String getGroupId(int position) {

                if (position < totalData.size()
                        && StringUtils.isNotEmpty(totalData.get(position).getSectionData())) {
                    return totalData.get(position).getSectionData();
                }
                return null;
            }

            @Override
            public String getGroupFirstLine(int position) {
                if (position < totalData.size()
                        && StringUtils.isNotEmpty(totalData.get(position).getSectionData())) {
                    return totalData.get(position).getSectionData();
                }
                return "";
            }

            @Override
            public String getActiveGroup() {
                return "今日";
            }
        });
        mBinding.rvMatchList.addItemDecoration(
                mItemDecor);
        mMatchListAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        mMatchListAdapter.isFirstOnly(false);
        mMatchListAdapter.setOnLoadMoreListener(this, mBinding.rvMatchList);
        mBinding.rvMatchList.setAdapter(mMatchListAdapter);
        mBinding.srlMatchList.setOnRefreshListener(this);
        mBinding.srlMatchList.setColorSchemeColors(mThemeColorMap.get(C.ATTRS.COLOR_PRIMARY));
    }

    /**
     * 初始化ProgressLayout
     */
    private void initProgressLayout() {
        mBinding.pl.setColor(mThemeColorMap.get(C.ATTRS.COLOR_TEXT_LIGHT)
                , mThemeColorMap.get(C.ATTRS.COLOR_PRIMARY));
        mBinding.pl.setRefreshClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading(mBinding.rvMatchList, mBinding.pl);
                mPresenter.addMatchListData(mCurrentDate, C.STATUS.INIT);
            }
        });
    }

    /**
     * 初始化未来和以往日志标志位
     *
     * @param currentDate
     */
    private void initFutureBeforeDate(String currentDate) {
        int year = DateTimeUitl.intGetYear(currentDate);
        int month = DateTimeUitl.intGetMonth(currentDate);
        int day = DateTimeUitl.intGetDay(currentDate);
        mFutureDate = DateTimeUitl.formatDateFromInt(year, month, day);
        mBeforeDate = DateTimeUitl.formatDateFromInt(year, month, day);
    }

}
