package com.example.firebase_clemenisle_ev.Classes;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String firstName, id, lastName, middleName, profileImage;
    private final List<SimpleTouristSpot> likedSpots = new ArrayList<>();
    private final List<Booking> bookingList = new ArrayList<>();
    private final List<Comment> comments = new ArrayList<>();
    private final List<Comment> upVotedComments = new ArrayList<>();
    private final List<Comment> downVotedComments = new ArrayList<>();
    private final List<Comment> reportedComments = new ArrayList<>();

    public User() {
    }

    public User(DataSnapshot dataSnapshot) {
        firstName = dataSnapshot.child("firstName").getValue(String.class);
        id = dataSnapshot.child("id").getValue(String.class);
        lastName = dataSnapshot.child("lastName").getValue(String.class);
        middleName = dataSnapshot.child("middleName").getValue(String.class);
        profileImage = dataSnapshot.child("profileImage").getValue(String.class);

        likedSpots.clear();
        DataSnapshot likedSpotSnapshot = dataSnapshot.child("likedSpots");
        if(likedSpotSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : likedSpotSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    SimpleTouristSpot likedSpot = dataSnapshot1.getValue(SimpleTouristSpot.class);
                    if(!likedSpot.isDeactivated()) {
                        likedSpots.add(likedSpot);
                    }
                }
            }
        }

        bookingList.clear();
        DataSnapshot bookingSnapshot = dataSnapshot.child("bookingList");
        if(bookingSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : bookingSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    Booking booking = new Booking(dataSnapshot1);
                    bookingList.add(booking);
                }
            }
        }

        comments.clear();
        DataSnapshot commentSnapshot = dataSnapshot.child("comments");
        if(commentSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : commentSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    Comment comment = dataSnapshot1.getValue(Comment.class);
                    comments.add(comment);
                }
            }
        }

        upVotedComments.clear();
        DataSnapshot upVotedCommentSnapshot = dataSnapshot.child("upVotedComments");
        if(upVotedCommentSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : upVotedCommentSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    for(DataSnapshot dataSnapshot2 : dataSnapshot1.getChildren()) {
                        if(dataSnapshot2.hasChildren()) {
                            Comment comment = dataSnapshot2.getValue(Comment.class);
                            upVotedComments.add(comment);
                        }
                    }
                }
            }
        }

        downVotedComments.clear();
        DataSnapshot downVotedCommentSnapshot = dataSnapshot.child("downVotedComments");
        if(downVotedCommentSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : downVotedCommentSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    for(DataSnapshot dataSnapshot2 : dataSnapshot1.getChildren()) {
                        if(dataSnapshot2.hasChildren()) {
                            Comment comment = dataSnapshot2.getValue(Comment.class);
                            downVotedComments.add(comment);
                        }
                    }
                }
            }
        }

        reportedComments.clear();
        DataSnapshot reportedCommentSnapshot = dataSnapshot.child("reportedComments");
        if(reportedCommentSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : reportedCommentSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    for(DataSnapshot dataSnapshot2 : dataSnapshot1.getChildren()) {
                        if(dataSnapshot2.hasChildren()) {
                            Comment comment = dataSnapshot2.getValue(Comment.class);
                            reportedComments.add(comment);
                        }
                    }
                }
            }
        }
    }

    public User(String firstName, String id, String lastName, String middleName) {
        this.firstName = firstName;
        this.id = id;
        this.lastName = lastName;
        this.middleName = middleName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public List<SimpleTouristSpot> getLikedSpots() {
        return likedSpots;
    }

    public List<Booking> getBookingList() {
        return bookingList;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<Comment> getUpVotedComments() {
        return upVotedComments;
    }

    public List<Comment> getDownVotedComments() {
        return downVotedComments;
    }

    public List<Comment> getReportedComments() {
        return reportedComments;
    }
}
