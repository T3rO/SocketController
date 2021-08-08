package com.tninteractive.socketcontroller.adddevice;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.tninteractive.socketcontroller.data.DataRepository;
import com.tninteractive.socketcontroller.FragmentListener;
import com.tninteractive.socketcontroller.R;
import com.tninteractive.socketcontroller.SocketControllerApp;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDeviceFragment.AddSocketListener} interface
 * to handle interaction events.
 * Use the {@link AddDeviceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDeviceFragment extends Fragment implements AddDeviceContract.View{

    private AddDeviceContract.Presenter mPresenter;

    private AddSocketListener mListener;

    private EditText nameEditText;
    private EditText ipEditText;
    private Button addButton;
    private Button cancelButton;

    public AddDeviceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddDeviceFragment.
     */
    public static AddDeviceFragment newInstance() {
        AddDeviceFragment fragment = new AddDeviceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_socket, container, false);

        nameEditText = view.findViewById(R.id.addSocketNameEditText);
        ipEditText = view.findViewById(R.id.addSocketIPEditText);
        addButton = view.findViewById(R.id.addSocketAddButton);
        cancelButton = view.findViewById(R.id.addSocketCancelButton);

        DataRepository repository = ((SocketControllerApp)getActivity().getApplication()).getRepository();
        mPresenter = new AddDevicePresenter(this, repository);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAddSocketButton(view);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCancelButton(view);
            }
        });

        return view;
    }

    public void onClickAddSocketButton(View view) {
        String name = nameEditText.getText().toString();
        String ip = ipEditText.getText().toString();

        mPresenter.addDevice(name, ip);

        mListener.previousFragment();
    }

    public void onClickCancelButton(View view){
        mListener.previousFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AddSocketListener) {
            mListener = (AddSocketListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        mListener.fragmentAttached();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.fragmentDetached();
        mListener = null;
    }

    @Override
    public void setPresenter(AddDeviceContract.Presenter presenter) {
        mPresenter = presenter;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface AddSocketListener extends FragmentListener {

    }
}
