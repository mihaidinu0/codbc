@Controller
@RequiredArgsConstructor
public class MagicController {

    private final MagicJwt magicJwt;
    private final UserRepository userRepo;
    private final CandidateRepository candidateRepo;

    @GetMapping("/magic/verify")
    public String verify(@RequestParam("jwt") String jwt, HttpSession session, Model model) {
        try {
            var data = magicJwt.parse(jwt);        // verifică semnătură + exp
            var email = data.email();
            var roundId = data.roundId();

            var user = userRepo.findById(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            var cid = new CandidateId(user.getEmail(), roundId);

            var cand = candidateRepo.findById(cid)
                    .orElseThrow(() -> new RuntimeException("Not a candidate for this round"));

            if (cand.isTokenUsed()) { model.addAttribute("error","Link already used"); return "invalid"; }

            cand.setTokenUsed(true);               // one-time per (user, round)
            candidateRepo.save(cand);

            session.setAttribute("userEmail", email);
            model.addAttribute("email", email);
            model.addAttribute("roundId", roundId);
            return "welcome";                      // sau redirect la UI-ul tău
        } catch (JwtException e) {
            model.addAttribute("error","Invalid or expired link");
            return "invalid";
        }
    }
}
