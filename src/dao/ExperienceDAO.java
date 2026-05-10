public class ExperienceDAO {
	void insertExperience(ReservationExperience re);
	void deleteExperience(int id);
	List<ReservationExperience> getExperiencesByReservation(int resId);
}
