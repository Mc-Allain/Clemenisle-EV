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
    private final List<Booking> taskList = new ArrayList<>();
    private boolean developer, admin, driver;
    private String plateNumber;
    private double iWallet;
    private final List<IWalletTransaction> transactionList = new ArrayList<>();
    private double amountToRemit = 0, amountToClaim = 0, incomeShare = 0;
    private final List<IncomeTransaction> amountToRemitTransactionList = new ArrayList<>();
    private final List<IncomeTransaction> amountToClaimTransactionList = new ArrayList<>();

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

        taskList.clear();
        DataSnapshot taskSnapshot = dataSnapshot.child("taskList");
        if(taskSnapshot.exists()) {
            for(DataSnapshot dataSnapshot1 : taskSnapshot.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    Booking booking = new Booking(dataSnapshot1);
                    taskList.add(booking);
                }
            }
        }

        if(dataSnapshot.child("developer").exists())
            developer = dataSnapshot.child("developer").getValue(Boolean.class);
        if(dataSnapshot.child("admin").exists())
            admin = dataSnapshot.child("admin").getValue(Boolean.class);
        if(dataSnapshot.child("driver").exists())
            driver = dataSnapshot.child("driver").getValue(Boolean.class);

        plateNumber = dataSnapshot.child("plateNumber").getValue(String.class);

        if(dataSnapshot.child("iWallet").exists())
            iWallet = dataSnapshot.child("iWallet").getValue(Double.class);

        transactionList.clear();
        DataSnapshot transactionListRef = dataSnapshot.child("iWalletTransactionList");
        if(transactionListRef.exists()) {
            for(DataSnapshot dataSnapshot1 : transactionListRef.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    IWalletTransaction transaction = dataSnapshot1.getValue(IWalletTransaction.class);
                    transactionList.add(transaction);
                }
            }
        }

        if(dataSnapshot.child("amountToRemit").exists())
            amountToRemit = dataSnapshot.child("amountToRemit").getValue(Double.class);
        if(dataSnapshot.child("amountToClaim").exists())
            amountToClaim = dataSnapshot.child("amountToClaim").getValue(Double.class);
        if(dataSnapshot.child("incomeShare").exists())
            incomeShare = dataSnapshot.child("incomeShare").getValue(Double.class);

        amountToRemitTransactionList.clear();
        DataSnapshot amountToRemitTransactionListRef = dataSnapshot.child("amountToRemitTransactionList");
        if(amountToRemitTransactionListRef.exists()) {
            for(DataSnapshot dataSnapshot1 : amountToRemitTransactionListRef.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    IncomeTransaction transaction = dataSnapshot1.getValue(IncomeTransaction.class);
                    amountToRemitTransactionList.add(transaction);
                }
            }
        }

        amountToClaimTransactionList.clear();
        DataSnapshot amountToClaimTransactionListRef = dataSnapshot.child("amountToClaimTransactionList");
        if(amountToClaimTransactionListRef.exists()) {
            for(DataSnapshot dataSnapshot1 : amountToClaimTransactionListRef.getChildren()) {
                if(dataSnapshot1.hasChildren()) {
                    IncomeTransaction transaction = dataSnapshot1.getValue(IncomeTransaction.class);
                    amountToClaimTransactionList.add(transaction);
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

    public List<Booking> getTaskList() {
        return taskList;
    }

    public boolean isDeveloper() {
        return developer;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isDriver() {
        return driver;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public double getIWallet() {
        return iWallet;
    }

    public List<IWalletTransaction> getTransactionList() {
        return transactionList;
    }

    public double getAmountToRemit() {
        return amountToRemit;
    }

    public double getAmountToClaim() {
        return amountToClaim;
    }

    public double getIncomeShare() {
        return incomeShare;
    }

    public List<IncomeTransaction> getAmountToRemitTransactionList() {
        return amountToRemitTransactionList;
    }

    public List<IncomeTransaction> getAmountToClaimTransactionList() {
        return amountToClaimTransactionList;
    }
}
