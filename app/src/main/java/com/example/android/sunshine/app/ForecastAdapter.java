package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    public static final String TAG = ForecastAdapter.class.getSimpleName();

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;

    private boolean useTodayLayout;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && useTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        //Select which layout to use
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        switch(viewType) {
            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_forecast;
                break;
            default:
                Log.wtf(TAG, "Unknown yet View Type was selected. Leaving as is.");
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);

        int viewType = getItemViewType(cursor.getPosition());

        switch(viewType) {
            case VIEW_TYPE_TODAY: {
                viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
                break;
            }

            case VIEW_TYPE_FUTURE_DAY: {
                viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
                break;
            }
            default:
                Log.wtf(TAG, "Unknown yet View Type was selected. Applying placeholder.");
                viewHolder.iconView.setImageResource(R.drawable.ic_launcher);
        }


        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);

        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);

        viewHolder.descriptionView.setText(description);

        //For accessibility we add description to icon field
        viewHolder.iconView.setContentDescription(description);

        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);

        viewHolder.highTempView.setText(Utility.formatTemperature(context, high));

        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(context, low));
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_text);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_text);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_text);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_text);
        }
    }
}